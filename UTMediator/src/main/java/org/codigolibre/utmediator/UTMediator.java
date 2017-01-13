package org.codigolibre.utmediator;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.Value;

import sun.misc.BASE64Encoder;


/**
 * WSO2 ESB Mediator.
 * Add to the current message a WSSecurity UsernameToken header (PasswordText type, nonce and timestamp)
 * Tag format:
 *        <usernametoken user="value|expresion" pass="value|expresion"/>
 *        user and pass: can be an expression or a value {$ctx:var1} or value1 
 *  examples:
 *  <usernametoken user="user1" pass="{$ctx:varWithPass1}"/>
 *  
 */
public class UTMediator extends AbstractMediator implements ManagedLifecycle {


	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	protected Value userValue;
	protected Value passswordValue;

	public Value getUserValue() {
		return userValue;
	}

	public void setUserValue(Value userValue) {
		this.userValue = userValue;
	}

	public Value getPassswordValue() {
		return passswordValue;
	}

	public void setPassswordValue(Value passswordValue) {
		this.passswordValue = passswordValue;
	}

	@Override
	public boolean mediate(MessageContext synCtx) {

		String user="";
		String pass="";
		boolean timeStamp=true;

		SynapseLog synLog = getLog(synCtx);

		if (synLog.isTraceOrDebugEnabled()) {
			synLog.traceOrDebug("Starting : UTMediator");
		}

		try {

			if (userValue != null) {
				user = userValue.evaluateValue(synCtx);
			}

			if (passswordValue != null) {
				pass = passswordValue.evaluateValue(synCtx);
			}

			// Inicialización y calculo elementos timestamp y usernametoken:
			// fechas, nonce, passwordDigest.
			SimpleDateFormat zulu = new SimpleDateFormat(DATE_FORMAT);
			zulu.setTimeZone(TimeZone.getTimeZone("UTC"));

			DatatypeFactory factory = DatatypeFactory.newInstance();
			Calendar now = Calendar.getInstance();
			Calendar fiveMinutesLater = Calendar.getInstance();
			fiveMinutesLater.add(GregorianCalendar.MINUTE, 5);

			String nowString = zulu.format(now.getTime());
			String fiveMinutesLaterString = zulu.format(fiveMinutesLater
					.getTime());

			// From the spec: Password_Digest = Base64 ( SHA-1 ( nonce + created
			// + password ) )
			// Make the nonce
			SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");
			rand.setSeed(System.currentTimeMillis());
			byte[] nonceBytes = new byte[16];
			rand.nextBytes(nonceBytes);

			// Make the password
			byte[] passwordBytes = pass.getBytes("UTF-8");

			// SHA-1 hash the bunch of it.
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			baos.write(nonceBytes);
			baos.write(nowString.getBytes());
			baos.write(passwordBytes);
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] digestedPassword = md.digest(baos.toByteArray());

			// Encode the password and nonce for sending
			String passwordB64 = new BASE64Encoder().encode(digestedPassword);

			String nonceB64 = new BASE64Encoder().encode(nonceBytes);

			String idBase = UUID.randomUUID().toString();

			org.apache.axiom.soap.SOAPEnvelope env = synCtx.getEnvelope();

			if (env == null) {
				synLog.auditError("UTMediator: Enveloped is null");
					throw new SynapseException(
						"UTMediator: Enveloped is null");
			}

			SOAPFactory omFactory = (SOAPFactory) synCtx.getEnvelope()
					.getOMFactory();

			org.apache.axiom.soap.SOAPHeader soapHeader = synCtx.getEnvelope()
					.getHeader();

			if (soapHeader == null) {
				soapHeader = omFactory.createSOAPHeader(env);
			}

			// Búsqueda de la header de seguridad o creación de una nueva

			QName securityQname = new QName(UTMediatorUtils.WS_SECEXT, "Security");

			OMNamespace securityNamespace = omFactory.createOMNamespace(
					UTMediatorUtils.WS_SECEXT, "wsse");
			OMNamespace securityUtilityNamespace = omFactory.createOMNamespace(
					UTMediatorUtils.WS_SECUTILITY, "wsu");

			OMElement securityHeader = soapHeader
					.getFirstChildWithName(securityQname);

			if (securityHeader == null) {

				SOAPHeaderBlock securityHeaderBlock = soapHeader
						.addHeaderBlock("Security", securityNamespace);
				securityHeaderBlock.declareNamespace(securityUtilityNamespace);
				// securityHeaderBlock.setMustUnderstand(true);
				securityHeader = securityHeaderBlock;
			}

			// Construcción del UsernameToken y guarda dentro de la header de
			// seguridad

			OMElement usernameToken = omFactory.createOMElement(new QName(
					UTMediatorUtils.WS_SECEXT, "UsernameToken", "wsse"));
			usernameToken.addAttribute("Id", "UsernameToken-" + idBase,
					securityUtilityNamespace);
			securityHeader.addChild(usernameToken);

			OMElement username = omFactory.createOMElement(new QName(UTMediatorUtils.WS_SECEXT,
					"Username", "wsse"));
			username.setText(user);
			usernameToken.addChild(username);

			OMElement password = omFactory.createOMElement(new QName(UTMediatorUtils.WS_SECEXT,
					"Password", "wsse"));
			password.addAttribute("Type", UTMediatorUtils.WS_USER_TOKEN_PROFILE, null);
			password.setText(pass);
			usernameToken.addChild(password);

			OMElement nonceOM = omFactory.createOMElement(new QName(UTMediatorUtils.WS_SECEXT,
					"Nonce", "wsse"));
			nonceOM.addAttribute("EncodingType", UTMediatorUtils.NONCE_EncodingType, null);
			nonceOM.setText(nonceB64);
			usernameToken.addChild(nonceOM);

			OMElement created = omFactory.createOMElement(new QName(
					UTMediatorUtils.WS_SECUTILITY, "Created", "wsu"));
			created.setText(zulu.format(now.getTime()));
			usernameToken.addChild(created);

			// incorporación del timeStamp al usernametoken
			if (timeStamp) {
				// TimeStamp

				OMElement timestamp = omFactory.createOMElement(new QName(
						UTMediatorUtils.WS_SECUTILITY, "Timestamp", "wsu"));
				timestamp.addAttribute("Id", "TS-" + idBase,
						securityUtilityNamespace);

				securityHeader.addChild(timestamp);

				OMElement createdTimeStamp = omFactory
						.createOMElement(new QName(UTMediatorUtils.WS_SECUTILITY, "Created",
								"wsu"));
				createdTimeStamp.setText(nowString);
				timestamp.addChild(createdTimeStamp);

				OMElement expired = omFactory.createOMElement(new QName(
						UTMediatorUtils.WS_SECUTILITY, "Expired", "wsu"));
				expired.setText(fiveMinutesLaterString);
				timestamp.addChild(expired);

			}

		} catch (Exception ex) {
			ex.printStackTrace();
			synLog.auditError("Error en UTMEdiator " + ex);
			throw new SynapseException(
					"Error occured in the mediation of the utmediator", ex,
					synLog);

		}

		if (synLog.isTraceOrDebugEnabled()) {
			synLog.traceOrDebug("END : UTMediator");
		}

		return true;

	}

	@Override
	public void destroy() {
	}

	@Override
	public void init(SynapseEnvironment arg0) {

	}

	public Set<QName> getHeaders() {
		Set<QName> set = new HashSet<QName>();
		// Make sure the '[{http://www.w3.org/2005/08/addressing}]Action' header
		// is handled in case the device set the 'MustUnderstand' attribute to
		// '1'
		set.add(new QName(UTMediatorUtils.WS_SECEXT, "Security", "wsse"));
		return set;

	}

}
