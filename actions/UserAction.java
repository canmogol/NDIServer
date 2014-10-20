import com.fererlab.action.BaseAction;
import com.fererlab.db.Transactional;
import com.fererlab.dto.Request;
import com.fererlab.dto.Response;
import com.fererlab.ndi.Wire;
import com.ndi.app.service.LDAPService;

public class UserAction extends BaseAction {

    @Wire
    private LDAPService ldapService;

    @Transactional
    Response doLogin(Request request) {
        if (ldapService.checkUsernamePassword(request.get("username"), request.get("password"))) {
            return Ok(request).add("data", "welcome").toResponse();
        } else {
            return Error(request, "could not logged in").toResponse();
        }
    }

}