package com.amida.saraswati.edifhir.service.mapper;

import com.amida.saraswati.edifhir.model.edi.component.x837.segment.CLM837;
import com.amida.saraswati.edifhir.model.edi.component.x837.segment.REF837;
import com.amida.saraswati.edifhir.model.edi.component.x837.segment.SV1837;
import com.amida.saraswati.edifhir.util.X12Util;
import com.imsweb.x12.Loop;
import com.imsweb.x12.Segment;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Identifier;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps an X12 claim loop, loop 2300, to a FHIR Claim resource.
 *
 * @author warren
 */
@Slf4j
public class ClaimMapper {
    private static final String LOOP_2400 = "2400";


    /**
     * Maps an X12 2300 loop to a FHIR Claim resource.
     *
     * @param loop2300 an X12 2300 loop object.
     * @return Claim.
     */
    public static Claim mapClaim(Loop loop2300) {
        Claim claim = new Claim();
        Segment clm = loop2300.getSegment("CLM");
        CLM837 clm837 = new CLM837(clm);
        setClaim(claim, clm837);
        Segment createDTP = loop2300.getSegment("DTP");
        // TODO: many DTP, etc.

        Segment ref = loop2300.getSegment("REF");
        if (ref != null) {
            REF837 ref837 = new REF837(ref);
            String claimNumber = ref837.getClaimNumber();
            if (claimNumber != null) {
                setClaimId(claim, claimNumber);
            }
        }
        // TODO: many REFs, etc


        Segment hi = loop2300.getSegment("HI");
        // TODO: many HIs, and HCP

        Loop loop2310A = loop2300.getLoop("2310A");
        Loop loop2310B = loop2300.getLoop("2310B");
        Loop loop2310C = loop2300.getLoop("2310C");
        Loop loop2310D = loop2300.getLoop("2310D");
        Loop loop2310E = loop2300.getLoop("2310E");
        Loop loop2310F = loop2300.getLoop("2310F");

        List<Loop> loop2400s = loop2300.getLoops().stream()
                .filter(l -> LOOP_2400.equals(l.getId()))
                .collect(Collectors.toList());
        loop2400s.forEach(l -> {
            Claim.ItemComponent item = createClaimItem(l);
            claim.addItem(item);
        });
        return claim;
    }

    private static Claim.ItemComponent createClaimItem(Loop loop2400) {
        Claim.ItemComponent item = new Claim.ItemComponent();
        Segment lineNumber = loop2400.getSegment("LX");
        Segment sv1 = loop2400.getSegment("SV1");
        SV1837 sv1837 = new SV1837(sv1);
        if (!StringUtils.isEmpty(sv1837.getSv101())) {
            item.getProductOrService().setText(sv1837.getSv101());
        }
        if (!StringUtils.isEmpty(sv1837.getSv102())) {
            try {
                item.setNet(X12Util.getMoneyObject(sv1837.getSv102()));
            } catch (NumberFormatException e) {
                log.error("Failed to convert line item amount. {}",
                        sv1837.getSv102(), e);
            }

        }
        if (!StringUtils.isEmpty(sv1837.getSv103())) {
            CodeableConcept code = new CodeableConcept();
            code.setText(sv1837.getSv103());
            item.getModifier().add(code);
        }

        item.setSequence(Integer.parseInt(lineNumber.getElementValue("LX01")));

        return item;
    }

    /**
     * Modifies the claim with a given X12 claim segment.
     *
     * @param claim FHIR Claim.
     * @param clm x12 claim segment.
     */
    private static void setClaim(Claim claim, CLM837 clm) {
        claim.setCreated(new Date());
        setClaimAmount(claim, clm.getClaimAmount());
    }

    /**
     * Modifies the claim id.
     *
     * @param claim FHIR Claim.
     * @param id claim id.
     */
    private static void setClaimId(Claim claim, String id) {
        Identifier claimId = claim.addIdentifier();
        claimId.setValue(id);
        claim.setId(id);
    }

    private static void setClaimAmount(Claim claim, String amtString) {
        try {
            claim.setTotal(X12Util.getMoneyObject(amtString));
        } catch (NumberFormatException e) {
            log.error("Invalid claim amount: {}", amtString);
        }
    }
}
