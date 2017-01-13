package org.codigolibre.utmediator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.synapse.config.xml.ValueSerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.mediators.Value;

public class UTMediatorUtils {

	private static ValueSerializer valueSerializer = new ValueSerializer();

	// root tag 
	public static final String UTMEDIATOR_TAG_NAME = "usernametoken";
	public static final QName UTMEDIATOR_QNAME = new QName(
			XMLConfigConstants.SYNAPSE_NAMESPACE, UTMEDIATOR_TAG_NAME);


	// User attribute
	public static final String USERNAME_ATT_TAG_NAME = "user";
	public static final QName USERNAME_ATT_QNAME = new QName(
			XMLConfigConstants.NULL_NAMESPACE, USERNAME_ATT_TAG_NAME);
	
	// Password attribute
	public static final String PASSWORD_ATT_TAG_NAME = "pass";
	public static final QName PASSWORD_ATT_QNAME = new QName(
			XMLConfigConstants.NULL_NAMESPACE, PASSWORD_ATT_TAG_NAME);
	
	// tag namespace, better we used the synapse namespace...
	public static String UTMEDIATOR_MEDIATOR_NAMESPACE = "urn:org:codigolibre:utmediator:type:v1.0.0";
	public static OMNamespace UTMEDIATOR_MEDIATOR_NAMESPACE_OM = null;
	

	// null Namespace
	public static final org.apache.axiom.om.OMNamespace nullNS = OMAbstractFactory
			.getOMFactory().createOMNamespace("", "");

	
	// generic namespaces
	public static String WS_SECEXT = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
	public static String WS_SECUTILITY = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
	public static String WS_USER_TOKEN_PROFILE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText";
	public static String NONCE_EncodingType = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary";

	
	
	/**
	 * Utility to change easy the namespace of the mediator
	 * 
	 * @param ns
	 */
	public static void setUTMediatorBaseNS(OMNamespace ns) {
		UTMEDIATOR_MEDIATOR_NAMESPACE_OM = ns;
		UTMEDIATOR_MEDIATOR_NAMESPACE = ns.getNamespaceURI();

	}

	/**
	 * Is this "element" a tag of type "tagQname"
	 * 
	 * @param element
	 * @param tagQname
	 * @return
	 */
	public static boolean isTag(OMElement element, String tagQname) {
		/**
		 * The Namespace and capitalizations are ignored in the comparison for
		 * easy editing tags inside the tag audit
		 */
		if (tagQname.equalsIgnoreCase(element.getLocalName())) {
			return true;
		} else
			return false;
	}

	/**
	 * Build a new tag of type "tagName"
	 * 
	 * @param fac
	 * @param tagName
	 * @return
	 */
	public static OMElement createTag(OMFactory fac, String tagName) {
		return fac.createOMElement(tagName, UTMEDIATOR_MEDIATOR_NAMESPACE_OM);
	}

	/**
	 * Add a new tag (tagChild) to this element "parentElement"
	 * 
	 * @param parentElement
	 * @param tagChild
	 */
	public static void addChildTag(OMElement parentElement, String tagChild) {
		OMElement campoElement = parentElement.getOMFactory().createOMElement(
				tagChild, UTMEDIATOR_MEDIATOR_NAMESPACE_OM);
		parentElement.addChild(campoElement);
	}
	/**
	 * Add a new attribute with value "value" to this element "parentElement"
	 * 
	 * @param parentElement
	 * @param attributeName
	 * @param value
	 */
	public static void addAttributeWithValue(OMElement parentElement,
			String attributeName, Value value) {
		valueSerializer.serializeValue(value, attributeName, parentElement);
	}

}
