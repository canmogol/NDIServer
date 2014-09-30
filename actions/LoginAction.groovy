import com.fererlab.dto.Request
import com.fererlab.dto.Response
import sample.ndi.app.action.GAction

public class LoginAction extends Script {

    Response login(GAction action, Request request) {
        if("acm".equals(request.get("username")) && "pass".equals(request.get("password"))){
            return action.Ok(request).add("data", "welcome").toResponse();
        }else{
            return action.Ok(request).add("data", "could not logged in!").toResponse();
        }
    }

    @Override
    Object run() {
        return new Object();
    }
}