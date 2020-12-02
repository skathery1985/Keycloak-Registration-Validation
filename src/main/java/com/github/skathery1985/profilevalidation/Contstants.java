package com.github.skathery1985.keycloak.profilevalidation;
import org.keycloak.services.messages.Messages;

/**
 * @author <a href="mailto:skathery1985@gmail.com">Saleh S.Kathery</a>
 * Profile and attributes validation
 * @version $Revision: 1 $
 * 2019-12-05
 */
public class Contstants {
    protected static final String CONF_PRP_PROVIDER_ID = "registration-profile-validation-id";
    public static final String CONF_PRP_PROVIDER_NAME = "Profile Full Validation";
    public static final String CONF_PRP_PROVIDER_HELP = "Validates email, first name, last name, attributes and stores them in user data.";
    public static final String CONF_PRP_PROVIDER_REF_CAT = "registration-profile-validation-cat";

    protected static final String CONF_PRP_ATTRIBUTES_NAME = "registration.profile.validation.attributes";
    protected static final String CONF_PRP_ATTRIBUTES_LABEL = "Attributes validations";
    protected static final String CONF_PRP_ATTRIBUTES_HELP = "no spaces between the ':' format is 'formName:Regex:IsReqiured:IsUnique'";
}