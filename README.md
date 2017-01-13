This custom WSO2 ESB Mediator insert a WSSecurity UserName Token in the current message.
The Username Token include timestamp y nonce.
 
The UTMediator has its own XML configuration. Tag format:

```
        <usernametoken user="value|expression" pass="value|expression"/>
 ```       
user and pass: can be an expression or a value
        
  Example:
  
  Inside sequence:
  ```
  	<property name="varWithPass1" value="password1"/>
    ..
    ..
   <usernametoken user="user1" pass="{$ctx:varWithPass1}"/>
   ```
   
   XML generated:
   
   ```
     <wsse:Security xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">
         <wsse:UsernameToken wsu:Id="UsernameToken-1aecc878-9e77-4bff-be86-5d8ec74af2d9">
            <wsse:Username>user1</wsse:Username>
            <wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">password1</wsse:Password>
            <wsse:Nonce EncodingType="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary">Gw/MFwVzQrT7SIsmn3KO7A==</wsse:Nonce>
            <wsu:Created>2017-01-13T09:36:04Z</wsu:Created>
         </wsse:UsernameToken>
         <wsu:Timestamp wsu:Id="TS-1aecc878-9e77-4bff-be86-5d8ec74af2d9">
            <wsu:Created>2017-01-13T09:36:04Z</wsu:Created>
            <wsu:Expired>2017-01-13T09:41:04Z</wsu:Expired>
         </wsu:Timestamp>
      </wsse:Security>
```
   
   Functional example in TestSoap-1.0.0.xml
