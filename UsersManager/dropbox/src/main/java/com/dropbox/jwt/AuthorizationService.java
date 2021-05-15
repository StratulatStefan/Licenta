package com.dropbox.jwt;

import java.util.Map;

public class AuthorizationService {
    public enum UserTypes {ALL};

    private static final String authorizationModel = "BEARER";
    private static final long sessionTime = 3600 * 1000;

    public static String generateUserIdentity(int id, String username, String userRole){
        JWT jwt = new JWT(id, username, userRole, sessionTime);
        return jwt.getJWT();
    }

    private Map<String, Object> decodeToken(String authorizationMessage) throws Exception{
        if(!authorizationMessage.toUpperCase().contains(authorizationModel)){
            throw new Exception("Invalid Authorization model. You should use " + authorizationModel);
        }
        String[] authorizationItems = authorizationMessage.split("\\s");
        String jwtToken = authorizationItems[1];

        if(!jwtToken.contains(".")){
            throw new Exception("Your token does not respect the Base64 JWT format! Include the delimitation between header and payload");
        }

        JWT jwt = new JWT(jwtToken);
        try {
            return jwt.decodeJWT();
        }
        catch (Exception exception){
            throw new Exception(exception.getMessage());
        }
    }

    private boolean checkJWTExpiration(String exceptionMessage){
        return exceptionMessage.contains("expired");
    }

    public Map<String, Object> userAuthorization(String authorizationValue, UserTypes[] requiredUserType) throws Exception{
        Map<String, Object> jwtData = null;
        try {
            jwtData = decodeToken(authorizationValue);
        }
        catch (Exception exception){
            if(checkJWTExpiration(exception.getMessage())){
                String exceptionMessage = "JWT Token expired!";
                System.out.println(exceptionMessage);
                throw new Exception(exceptionMessage);
            }
        }

        for(UserTypes userType : requiredUserType){
            if(userType == UserTypes.ALL || UserTypes.valueOf((String)jwtData.get("role")) == userType){
                System.out.println("User successfully authorized!");
                return jwtData;
            }
        }
        throw new Exception("Role exception! Your user type is not allowed to take this action!");
    }
}
