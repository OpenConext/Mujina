package mujina.idp;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



import mujina.saml.SAMLAttribute;



public class IdpChange {


public List<SAMLAttribute> changeRvOkta(List<SAMLAttribute> currentAttributeList) {
List<SAMLAttribute> newAttributeList = new ArrayList<>();
//List<String> groupList = new ArrayList<>();
//groupList.add("Everyone");
newAttributeList.add(new SAMLAttribute("groups", Arrays.asList("Everyone")));//In okta each user has at least one group called Everyone.
for (int i = 0; i < currentAttributeList.size(); i++) {
String name = currentAttributeList.get(i).getName();
List<String> value = currentAttributeList.get(i).getValues();
if(name.contains("givenName"))
newAttributeList.add(new SAMLAttribute("firstName", value));
else if(name.contains("sn"))
newAttributeList.add(new SAMLAttribute("lastName", value));
else if(name.contains("uid"))
newAttributeList.add(new SAMLAttribute("userId", value));
//else if(name.contains("isMemberOf"))
//groupList.add(value.toString());
//temp.add(new SAMLAttribute("groups", value));
}
//temp.add(new SAMLAttribute("groups", groupList));
return newAttributeList;
}

public List<SAMLAttribute> changeRvAdfs(List<SAMLAttribute> currentAttributeList) {
List<SAMLAttribute> newAttributeList = new ArrayList<>();
//List<String> groupList = new ArrayList<>();
//groupList.add("default Group");
newAttributeList.add(new SAMLAttribute("http://schemas.xmlsoap.org/claims/Group", Arrays.asList("default Group")));
for (int i = 0; i < currentAttributeList.size(); i++) {
String name = currentAttributeList.get(i).getName();
List<String> value = currentAttributeList.get(i).getValues();
if(name.contains("givenName")){
newAttributeList.add(new SAMLAttribute("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name", value));
newAttributeList.add(new SAMLAttribute("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress", value));
}
else if(name.contains("sn"))
newAttributeList.add(new SAMLAttribute("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname", value));
//else if(name.contains("isMemberOf"))
//groupList.add(value.toString());
//temp.add(new SAMLAttribute("http://schemas.xmlsoap.org/claims/Group", value));
else if(name.contains("uid"))
newAttributeList.add(new SAMLAttribute("http://schemas.microsoft.com/ws/2008/06/identity/claims/primarysid", value));
}
//temp.add(new SAMLAttribute("http://schemas.xmlsoap.org/claims/Group", groupList));
newAttributeList.add(new SAMLAttribute("http://schemas.microsoft.com/ws/2008/06/identity/claims/groupsid", Arrays.asList("S-1-5-21-1752647966-813121237-1802144934-444")));
return newAttributeList;
}


public List<SAMLAttribute> changeRvAzure(List<SAMLAttribute> currentAttributeList) {
List<SAMLAttribute> newAttributeList = new ArrayList<>();
for (int i = 0; i < currentAttributeList.size(); i++) {
String name = currentAttributeList.get(i).getName();
List<String> value = currentAttributeList.get(i).getValues();
if(name.contains("sn"))
newAttributeList.add(new SAMLAttribute("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname", value));
else if(name.contains("uid")){
newAttributeList.add(new SAMLAttribute("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name", value));
newAttributeList.add(new SAMLAttribute("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress", value));
}
else if(name.contains("isMemberOf"))
newAttributeList.add(new SAMLAttribute("groups", value));
else if(name.contains("displayName"))
newAttributeList.add(new SAMLAttribute("http://schemas.microsoft.com/identity/claims/displayname", value));
else if(name.contains("givenName"))
newAttributeList.add(new SAMLAttribute("http://schemas.microsoft.com/identity/claims/givenname", value));

}
newAttributeList.add(new SAMLAttribute("http://schemas.microsoft.com/ws/2008/06/identity/claims/role", Arrays.asList("admin")));
newAttributeList.add(new SAMLAttribute("http://schemas.microsoft.com/ws/2008/06/identity/claims/groups", Arrays.asList("b4e61a24-a485-4363-b6b3-de0247d7db7c", "5ee7c6ef-fbca-40cb-a0b2-2f831cba1399", "f3a3c151-bcf2-4c18-a2f0-7284eb48d86d")));
newAttributeList.add(new SAMLAttribute("http://schemas.microsoft.com/identity/claims/tenantid", Arrays.asList("tenantid default")));
newAttributeList.add(new SAMLAttribute("http://schemas.microsoft.com/identity/claims/objectidentifier", Arrays.asList("objectidentifier default")));
newAttributeList.add(new SAMLAttribute("http://schemas.microsoft.com/identity/claims/identityprovider", Arrays.asList("identityprovider default")));
newAttributeList.add(new SAMLAttribute("http://schemas.microsoft.com/identity/claims/authnmethodsreferences", Arrays.asList("authnmethodsreferences default")));
return newAttributeList;
}


public List<SAMLAttribute> changeRvOneLogin(List<SAMLAttribute> currentAttributeList) {
List<SAMLAttribute> newAttributeList = new ArrayList<>();
newAttributeList.add(new SAMLAttribute("groups", Arrays.asList("default group")));//In onelogin groups can be empty.
for (int i = 0; i < currentAttributeList.size(); i++) {
String name = currentAttributeList.get(i).getName();
List<String> value = currentAttributeList.get(i).getValues();
if(name.contains("givenName"))
newAttributeList.add(new SAMLAttribute("firstName", value));
else if(name.contains("sn"))
newAttributeList.add(new SAMLAttribute("lastName", value));
else if(name.contains("uid")){
newAttributeList.add(new SAMLAttribute("userID", value));
newAttributeList.add(new SAMLAttribute("email", value));
}

else if(name.contains("isMemberOf"))
newAttributeList.add(new SAMLAttribute("groups", value));
}
return newAttributeList;
}



public List<SAMLAttribute> changeRvPing(List<SAMLAttribute> currentAttributeList) {
List<SAMLAttribute> newAttributeList = new ArrayList<>();
for (int i = 0; i < currentAttributeList.size(); i++) {
String name = currentAttributeList.get(i).getName();
List<String> value = currentAttributeList.get(i).getValues();
if(name.contains("givenName"))
newAttributeList.add(new SAMLAttribute("firstName", value));
else if(name.contains("sn"))
newAttributeList.add(new SAMLAttribute("lastName", value));
else if(name.contains("uid"))
newAttributeList.add(new SAMLAttribute("email", value));

else if(name.contains("isMemberOf")) {
for (int j = 0; j < value.size(); j++)//In Ping all groups has @directory postfix.
value.set(j, value.get(j)+"@directory");
newAttributeList.add(new SAMLAttribute("memberOf", value));
}
}
return newAttributeList;
}

}
