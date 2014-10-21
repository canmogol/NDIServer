import com.fererlab.action.BaseAction;
import com.fererlab.db.Transactional;
import com.fererlab.dto.Request;
import com.fererlab.dto.Response;
import com.fererlab.ndi.Wire;
import com.ndi.app.service.LDAPService;

public class UserAction extends BaseAction {

    @Wire
    private LDAPService ldapService;

    Response showUser(Request r) {
        return Ok(r).add("data", r.getSession().getUser()).toResponse();
    }

    Response doLogout(Request r) {
        r.getSession().getUser().logout();
        return Ok(r).add("data", r.getSession().getUser()).toResponse();
    }

    @Transactional
    Response doLogin(Request r) {
        if (ldapService.checkUsernamePassword(r.get("username"), r.get("password"))) {
            if (!r.getSession().getUser().isLogged()) {
                r.getSession().getUser().setLogged(true);
                r.getSession().getUser().setUsername(r.get("username"));
                r.getSession().getUser().setSessionId(r.getSession().getSessionId());
                r.getSession().getUser().getGroups().add("admin");
                r.getSession().getUser().getGroups().add("user");
            }
            return Ok(r).add("data", "welcome").toResponse();
        } else {
            return Error(r, "could not logged in").toResponse();
        }
    }

}