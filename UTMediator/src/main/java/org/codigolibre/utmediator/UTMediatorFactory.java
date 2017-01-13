package org.codigolibre.utmediator;

import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorFactory;
import org.apache.synapse.config.xml.ValueFactory;

/**
 * UTMediator tag parser
 * @author mantos
 *
 */
public class UTMediatorFactory extends AbstractMediatorFactory {
	/*
	 * Tag format:
	 *        <usernametoken user="value|expresion" pass="value|expresion"/>
	 *        user and pass: can be an expression or a value {$ctx:var1} or value1 
	 *  examples:
	 *  <usernametoken user="user1" pass="{$ctx:varWithPass1}"/>
	 */
	
	private static Log log = LogFactory.getLog(UTMediatorFactory.class);

	@Override
	public QName getTagQName() {
		return UTMediatorUtils.UTMEDIATOR_QNAME;
	}

	@Override
	protected Mediator createSpecificMediator(OMElement element,
			Properties props) {

		if (log.isDebugEnabled() || log.isTraceEnabled()) {
			log.debug("Parsing UTMediator tag");
		}

		if (!UTMediatorUtils
				.isTag(element, UTMediatorUtils.UTMEDIATOR_TAG_NAME)) {
			handleException("Unable to create the UTMediator tag -> "
					+ element.getQName() + ":" + element.getLocalName()
					+ "Unexpected element as the UTMediator configuration");
		}

		
		UTMediator utMediator = new UTMediator();

		ValueFactory valueFactory = new ValueFactory();

		OMAttribute username = element
				.getAttribute(UTMediatorUtils.USERNAME_ATT_QNAME);

	
		OMAttribute password = element
				.getAttribute(UTMediatorUtils.PASSWORD_ATT_QNAME);
		
		
		if (username != null) {
			String temp = username.getAttributeValue();
			if (!StringUtils.isBlank(temp)) {
				utMediator
						.setUserValue(valueFactory.createValue(
								UTMediatorUtils.USERNAME_ATT_QNAME.getLocalPart(),
								element));
			}
		}

		if (password != null) {
			String temp = password.getAttributeValue();
			if (!StringUtils.isBlank(temp)) {
				utMediator
						.setPassswordValue(valueFactory.createValue(
								UTMediatorUtils.PASSWORD_ATT_QNAME.getLocalPart(),
								element));
			}
		}

		return utMediator;
	}

}
