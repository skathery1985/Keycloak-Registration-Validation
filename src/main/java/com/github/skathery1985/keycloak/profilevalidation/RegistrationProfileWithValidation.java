package com.github.skathery1985.keycloak.profilevalidation;
/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.authentication.forms.RegistrationPage;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.messages.Messages;
import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static com.github.skathery1985.keycloak.profilevalidation.Contstants.*;
import static org.keycloak.models.Constants.USER_ATTRIBUTES_PREFIX;
import static org.keycloak.services.validation.Validation.*;

/**
 * @author <a href="mailto:skathery1985@gmail.com">Saleh S.Kathery</a>
 * Profile and attributes validation
 * @version $Revision: 1 $
 * 2019-12-05
 */
public class RegistrationProfileWithValidation implements FormAction, FormActionFactory {

    @Override
    public String getHelpText() {
        return Contstants.CONF_PRP_PROVIDER_HELP;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public void validate(ValidationContext context) {

        AuthenticatorConfigModel configModel = context.getAuthenticatorConfig();
        List<String> attributes = new ArrayList<>(Arrays.asList(configModel.getConfig().get(Contstants.CONF_PRP_ATTRIBUTES_NAME).split("##")));

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        List<FormMessage> errors = new ArrayList<>();

        context.getEvent().detail(Details.REGISTER_METHOD, "form");
        String eventError = Errors.INVALID_REGISTRATION;

        if (isBlank(formData.getFirst((RegistrationPage.FIELD_FIRST_NAME)))) {
            errors.add(new FormMessage(RegistrationPage.FIELD_FIRST_NAME, Messages.MISSING_FIRST_NAME));
        }

        if (isBlank(formData.getFirst((RegistrationPage.FIELD_LAST_NAME)))) {
            errors.add(new FormMessage(RegistrationPage.FIELD_LAST_NAME, Messages.MISSING_LAST_NAME));
        }

        String email = formData.getFirst(FIELD_EMAIL);

        boolean emailValid = true;
        if (isBlank(email)) {
            errors.add(new FormMessage(RegistrationPage.FIELD_EMAIL, Messages.MISSING_EMAIL));
            emailValid = false;
        } else if (!isEmailValid(email)) {
            context.getEvent().detail(Details.EMAIL, email);
            errors.add(new FormMessage(RegistrationPage.FIELD_EMAIL, Messages.INVALID_EMAIL));
            emailValid = false;
        }

        //INVALID ATTRIBUTES
        for (String item : attributes) {

            String Attribute = item.split(":", 4)[0];
            String Regex = item.split(":", 4)[1];
            boolean IsReqiured = Boolean.parseBoolean(item.split(":", 4)[2]);
            boolean IsUnique = Boolean.parseBoolean(item.split(":", 4)[3]);

            String AttributeValue = formData.getFirst(USER_ATTRIBUTES_PREFIX + Attribute);
            Pattern ATTRIBUTE_PATTERN = Pattern.compile(Regex);

            //is reqiured
            if (IsReqiured && isBlank(AttributeValue)) {
                errors.add(new FormMessage(USER_ATTRIBUTES_PREFIX + Attribute, MISSING_ATTRIBUTE + Attribute));
                continue;
            }

            //is invalid
            if (!isBlank(AttributeValue) && !ATTRIBUTE_PATTERN.matcher(AttributeValue).matches()) {
                errors.add(new FormMessage(USER_ATTRIBUTES_PREFIX + Attribute, INVALID_ATTRIBUTE + Attribute));
                continue;
            }

            //is unique
            List<UserModel> userModels = context.getSession().users().searchForUserByUserAttribute(Attribute, AttributeValue, context.getRealm());
            if (IsUnique && !isBlank(AttributeValue) && userModels != null && !userModels.isEmpty()) {
                eventError = EXISTS_ATTRIBUTE + Attribute;
                formData.remove(USER_ATTRIBUTES_PREFIX + Attribute);
                context.getEvent().detail(Attribute, AttributeValue);
                errors.add(new FormMessage(USER_ATTRIBUTES_PREFIX + Attribute, EXISTS_ATTRIBUTE + Attribute));
            }
        }

        if (emailValid &&
                !context.getRealm().isDuplicateEmailsAllowed() &&
                (context.getSession().users().getUserByEmail(email, context.getRealm()) != null ||
                        context.getSession().users().getUserByUsername(email, context.getRealm()) != null)) {

            eventError = Errors.EMAIL_IN_USE;
            formData.remove(FIELD_EMAIL);

            context.getEvent().detail(Details.EMAIL, email);
            errors.add(new FormMessage(RegistrationPage.FIELD_EMAIL, Messages.EMAIL_EXISTS));
        }

        if (errors.size() > 0) {
            context.error(eventError);
            context.validationError(formData, errors);
            return;
        }

        context.success();
    }

    @Override
    public void success(FormContext context) {
        UserModel user = context.getUser();
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        user.setFirstName(formData.getFirst(RegistrationPage.FIELD_FIRST_NAME));
        user.setLastName(formData.getFirst(RegistrationPage.FIELD_LAST_NAME));
        user.setEmail(formData.getFirst(RegistrationPage.FIELD_EMAIL));
    }

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {
        // complete
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }


    @Override
    public void close() {

    }

    @Override
    public String getDisplayType() {
        return Contstants.CONF_PRP_PROVIDER_NAME;
    }

    @Override
    public String getReferenceCategory() {
        return Contstants.CONF_PRP_PROVIDER_REF_CAT;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };
    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }
    @Override
    public FormAction create(KeycloakSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public String getId() {
        return Contstants.CONF_PRP_PROVIDER_ID;
    }


    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = new ArrayList<>();

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(Contstants.CONF_PRP_ATTRIBUTES_NAME);
        property.setLabel(Contstants.CONF_PRP_ATTRIBUTES_LABEL);
        property.setType(ProviderConfigProperty.MULTIVALUED_STRING_TYPE);
        property.setHelpText(Contstants.CONF_PRP_ATTRIBUTES_HELP);
        CONFIG_PROPERTIES.add(property);
    }
}