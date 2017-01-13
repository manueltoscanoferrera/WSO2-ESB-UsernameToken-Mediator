package org.codigolibre.utmediator;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorSerializer;

/**
 * UTMediator Serializer
 * 
 * @author mantos
 * 
 */
public class UTMediatorSerializer extends AbstractMediatorSerializer {
	/*
	 * Tag format: <usernametoken user="value|expresion"
	 * pass="value|expresion"/> user and pass: can be an expression or a value
	 * {$ctx:var1} or value1 examples: <usernametoken user="user1"
	 * pass="{$ctx:varWithPass1}"/>
	 */

	@Override
	public String getMediatorClassName() {
		return UTMediator.class.getName();
	}

	@Override
	protected OMElement serializeSpecificMediator(Mediator mediator) {

		if (log.isDebugEnabled() || log.isTraceEnabled()) {
			log.debug("START: UTMediator Serialize Tag");
		}

		if (!(mediator instanceof UTMediator)) {
			handleException("Unsupported mediator passed in for serialization: "
					+ mediator.getType());
		}

		UTMediator utMediator = (UTMediator) mediator;
		// more comfortable working with synapse namespace than "utmediator"
		// namespace
		UTMediatorUtils.setUTMediatorBaseNS(synNS);

		OMElement utmediatorElement = UTMediatorUtils.createTag(fac,
				UTMediatorUtils.UTMEDIATOR_TAG_NAME);

		if (utMediator.getUserValue() != null) {

			UTMediatorUtils.addAttributeWithValue(utmediatorElement,
					UTMediatorUtils.USERNAME_ATT_TAG_NAME,
					utMediator.getUserValue());

		} else {
			utmediatorElement.addAttribute("user", "", null);

		}

		if (utMediator.getPassswordValue() != null) {

			UTMediatorUtils.addAttributeWithValue(utmediatorElement,
					UTMediatorUtils.PASSWORD_ATT_TAG_NAME,
					utMediator.getPassswordValue());
		} else {

			utmediatorElement.addAttribute("pass", "", null);
		}

		if (log.isDebugEnabled() || log.isTraceEnabled()) {
			log.debug("END: UTMediator Serialize Tag");
		}
		return utmediatorElement;
	}

}
