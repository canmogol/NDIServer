import com.fererlab.action.BaseAction
import com.fererlab.action.ModelAction
import com.fererlab.dto.Request
import com.fererlab.dto.Response
import com.fererlab.ndi.Wire
import com.ndi.app.model.Department
import com.ndi.app.service.LDAPService

public class DepartmentAction extends Script {

    @Wire
    private LDAPService ldapService;

    Response doLogin(BaseAction action, Request request) {
        if (ldapService.checkUsernamePassword(request.get("username"), request.get("password"))) {
            return action.Ok(request).add("data", "welcome").toResponse();
        } else {
            return action.Error(request, "could not logged in").toResponse();
        }
    }

    Response test(BaseAction action, Request request) {
        return action.Ok(request).add("data", "hi there").toResponse();
    }

    Response listFor(BaseAction action, Request request) {
        ModelAction<Department> modelAction = new ModelAction<Department>(Department.class);
        List<Department> list = modelAction.findAll();
        return action.Ok(request).add("data", list).add("test", "123").toResponse();
    }

    @Override
    Object run() {
        return new Object();
    }
}