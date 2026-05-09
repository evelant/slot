import { readFileSync } from "node:fs";
import { inflateRawSync } from "node:zlib";

export interface ZipEntry {
  name: string;
  compressionMethod: number;
  compressedSize: number;
  uncompressedSize: number;
  localHeaderOffset: number;
}

export class ZipArchive {
  private constructor(
    private readonly bytes: Buffer,
    private readonly entriesByName: Map<string, ZipEntry>,
  ) {
  }

  static open(path: string): ZipArchive {
    return ZipArchive.fromBuffer(readFileSync(path));
  }

  static fromBuffer(bytes: Buffer): ZipArchive {
    const entries = readCentralDirectory(bytes);
    return new ZipArchive(bytes, new Map(entries.map((entry) => [entry.name, entry])));
  }

  entries(): ZipEntry[] {
    return [...this.entriesByName.values()];
  }

  entryNames(): string[] {
    return [...this.entriesByName.keys()];
  }

  has(name: string): boolean {
    return this.entriesByName.has(name);
  }

  readText(name: string): string | null {
    const data = this.read(name);
    return data ? data.toString("utf8") : null;
  }

  readJson<T>(name: string): T | null {
    const text = this.readText(name);
    if (text === null) return null;
    try {
      return JSON.parse(text) as T;
    } catch {
      return null;
    }
  }

  read(name: string): Buffer | null {
    const entry = this.entriesByName.get(name);
    if (!entry) return null;
    const local = entry.localHeaderOffset;
    if (this.bytes.readUInt32LE(local) !== 0x04034b50) {
      throw new Error(`zip local header signature mismatch for ${name}`);
    }
    const nameLength = this.bytes.readUInt16LE(local + 26);
    const extraLength = this.bytes.readUInt16LE(local + 28);
    const dataStart = local + 30 + nameLength + extraLength;
    const compressed = this.bytes.subarray(dataStart, dataStart + entry.compressedSize);
    if (entry.compressionMethod === 0) {
      return Buffer.from(compressed);
    }
    if (entry.compressionMethod === 8) {
      return inflateRawSync(compressed);
    }
    throw new Error(`unsupported zip compression method ${entry.compressionMethod} for ${name}`);
  }
}

function readCentralDirectory(bytes: Buffer): ZipEntry[] {
  const eocdOffset = findEndOfCentralDirectory(bytes);
  const entryCount = bytes.readUInt16LE(eocdOffset + 10);
  const centralDirectoryOffset = bytes.readUInt32LE(eocdOffset + 16);
  const entries: ZipEntry[] = [];
  let cursor = centralDirectoryOffset;
  for (let i = 0; i < entryCount; i++) {
    if (bytes.readUInt32LE(cursor) !== 0x02014b50) {
      throw new Error(`zip central directory signature mismatch at ${cursor}`);
    }
    const compressionMethod = bytes.readUInt16LE(cursor + 10);
    const compressedSize = bytes.readUInt32LE(cursor + 20);
    const uncompressedSize = bytes.readUInt32LE(cursor + 24);
    const nameLength = bytes.readUInt16LE(cursor + 28);
    const extraLength = bytes.readUInt16LE(cursor + 30);
    const commentLength = bytes.readUInt16LE(cursor + 32);
    const localHeaderOffset = bytes.readUInt32LE(cursor + 42);
    const name = bytes.subarray(cursor + 46, cursor + 46 + nameLength).toString("utf8");
    cursor += 46 + nameLength + extraLength + commentLength;
    if (name.endsWith("/")) continue;
    entries.push({
      name,
      compressionMethod,
      compressedSize,
      uncompressedSize,
      localHeaderOffset,
    });
  }
  return entries;
}

function findEndOfCentralDirectory(bytes: Buffer): number {
  const min = Math.max(0, bytes.length - 0xffff - 22);
  for (let offset = bytes.length - 22; offset >= min; offset--) {
    if (bytes.readUInt32LE(offset) === 0x06054b50) {
      return offset;
    }
  }
  throw new Error("zip end-of-central-directory record not found");
}
