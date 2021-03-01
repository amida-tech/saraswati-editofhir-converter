/* warren created on 2/24/21 inside the package - com.amida.saraswati.edifhir.model.edi.component.x837 */
package com.amida.saraswati.edifhir.model.edi.component.x837;

import com.amida.saraswati.edifhir.model.edi.component.x12segment.NM1;
import com.amida.saraswati.edifhir.model.edi.component.x12segment.REF;
import lombok.Data;

@Data
public class Loop2310A {
    private NM1 referingProviderName;
    private REF refProvifder2ndId;

}
