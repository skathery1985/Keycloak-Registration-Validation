# Keycloak Registration Validation

Validate user profile nad attributes server-side

# Features
* Required: choose if you want any attribute is mandatory.
* Unique: check if the data isn't already exists (like mobile number).
* Pattern: Specifies a regular expression that defines a pattern the entered data needs to follow.
* Localization: you can localize error messages for invalid entered data.

Tested on Keycloak 7.0.0

### Example for Registration flow
![Example for Registration flow](example-registration-flow.png)
 
 ### Change the bindings for Registration flow
![Example for Registration flow](example-binding-registration-flow.png)
 
### Example for configurations
I didn't find a better why to apply this, so
our recipe is attributeName:attributeRegex:isRequired:isUnique\
```e.g: mobile:^[0-9]*$:true:true```\
\
 ![Example for Registration validation configuration](example-registration-validation-config.png)
 
 ## Example of adding a custom attribute in themes/YourTheme/login/register.ftl\
 Don't forget to add a validation client-side duh
 ```html
 <div class="${properties.kcFormGroupClass!} ${messagesPerField.printIfExists('mobile',properties.kcFormGroupErrorClass!)}">
   <div class="${properties.kcLabelWrapperClass!}">
      <label for="mobile" class="${properties.kcLabelClass!}">${msg("mobile")}</label>
   </div>
   <div class="${properties.kcInputWrapperClass!}">
      <input type="text" id="user.attributes.mobile" class="${properties.kcInputClass!}" name="user.attributes.mobile" value="${(register.formData.mobile!'')}"  />
   </div>
</div>
 ```
