package dev.imagio.slot.inventory.query;

import dev.imagio.slot.inventory.core.ItemIdentity;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CarriedIdentityCountsTest {
    @Test
    void itemOnlyCarriedStacksSatisfyOlderExactTargets() {
        CarriedIdentityCounts counts = new CarriedIdentityCounts(Map.of(
                ItemIdentity.of("mod:hammer"), 1));

        assertEquals(1, counts.count(ItemIdentity.exact("mod:hammer", "{Damage:0,Mode:\"old\"}")));
    }

    @Test
    void itemOnlyTargetsCountEveryCarriedVariantOfTheItem() {
        CarriedIdentityCounts counts = new CarriedIdentityCounts(Map.of(
                ItemIdentity.exact("mod:flask", "{Fluid:\"water\"}"), 2,
                ItemIdentity.exact("mod:flask", "{Fluid:\"brine\"}"), 1));

        assertEquals(3, counts.count(ItemIdentity.of("mod:flask")));
        assertEquals(2, counts.count(ItemIdentity.exact("mod:flask", "{Fluid:\"water\"}")));
        assertEquals(0, counts.count(ItemIdentity.exact("mod:flask", "{Fluid:\"milk\"}")));
    }

    @Test
    void patchouliBookTargetsCountCarriedCopiesWithIncidentalData() {
        CarriedIdentityCounts counts = new CarriedIdentityCounts(Map.of(
                ItemIdentity.exact(
                        "patchouli:guide_book",
                        "{display:{Name:\"TerraFirmaGreg Guide\"},\"patchouli:book\":\"tfc:field_guide\"}"),
                1));

        assertEquals(1, counts.count(ItemIdentity.exact(
                "patchouli:guide_book",
                "{\"patchouli:book\":\"tfc:field_guide\"}")));
        assertEquals(0, counts.count(ItemIdentity.exact(
                "patchouli:guide_book",
                "{\"patchouli:book\":\"ae2:guide\"}")));
    }

    @Test
    void patchouliBookTargetsCountCarriedCopiesWithNestedCustomDataSelector() {
        CarriedIdentityCounts counts = new CarriedIdentityCounts(Map.of(
                ItemIdentity.exact(
                        "patchouli:guide_book",
                        "{minecraft:custom_data=>CustomData[{display:{Name:\"TerraFirmaGreg Guide\"},\"patchouli:book\":\"tfc:field_guide\"}]}"),
                1));

        assertEquals(1, counts.count(ItemIdentity.exact(
                "patchouli:guide_book",
                "{\"patchouli:book\":\"tfc:field_guide\"}")));
        assertEquals(0, counts.count(ItemIdentity.exact(
                "patchouli:guide_book",
                "{minecraft:custom_data=>{\"patchouli:book\":\"ae2:guide\"}}")));
    }

    @Test
    void tfcFoodTargetsCountCarriedCopiesAcrossPreservedState() {
        CarriedIdentityCounts counts = new CarriedIdentityCounts(Map.of(
                ItemIdentity.exact(
                        "tfc:food/banana",
                        "{tfc:food=>Food[creationDate=100,rotten=false,traits=[]]}"),
                4));

        assertEquals(4, counts.count(ItemIdentity.exact(
                "tfc:food/banana",
                "{tfc:food=>Food[creationDate=200,rotten=false,traits=[preserved]]}")));
        assertEquals(4, counts.count(ItemIdentity.exact(
                "tfc:food/banana",
                "{ForgeCaps:{\"tfc:food\":{creationDate:200,traits:[\"tfc:preserved\"]}}}")));
    }
}
