import com.fererlab.action.BaseAction;
import com.fererlab.action.ModelAction;
import com.fererlab.db.Transactional;
import com.fererlab.dto.Request;
import com.fererlab.dto.Response;
import com.fererlab.ndi.Wire;
import com.fererlab.session.SessionUser;
import com.ndi.app.model.Department;
import com.ndi.app.service.LDAPService;

import java.util.List;

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
            SessionUser user = r.getSession().getUser();
            user.setLogged(true);
            user.setUsername(r.get("username"));
            if (user.getSessionId() == null) {
                user.setSessionId(r.getSession().getSessionId());
            }
            user.getGroups().add("admin");
            user.getGroups().add("user");

            ModelAction<Department> modelAction = new ModelAction<Department>(Department.class);
            List<Department> departments = modelAction.findAll();
            user.getProperties().put("departments", departments);
            return Ok(r).add("data", user).add("welcome", message("welcome")).toResponse();
        } else {
            return Error(r, "could not logged in").toResponse();
        }
    }

}