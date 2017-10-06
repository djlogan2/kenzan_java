package david.logan.kenzan.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import david.logan.kenzan.db.Employee;
import david.logan.kenzan.db.KenzanDAO;
import david.logan.kenzan.jwt.JwtToken;
import david.logan.kenzan.jwt.Login;
import david.logan.kenzan.jwt.LoginResponse;

@RestController
@RequestMapping("/rest")
public class KenzanRestServiceController {
	@Autowired
	KenzanDAO dbDAO;
	@RequestMapping("/get_emp")
	public Employee get_emp(@RequestParam(value="id", defaultValue="-1") int id)
	{
		return dbDAO.getEmployee(id);
	}
	
	@RequestMapping("/get_all")
	public List<Employee> get_all()
	{
		return dbDAO.getEmployees();
	}

	@RequestMapping(value = "/add_emp", method = RequestMethod.POST)
	@PreAuthorize("hasRole('ADD_EMP')")
	public ErrorResponse add_emp(@RequestBody Employee newEmployee)
	{
		int id = dbDAO.addEmployee(newEmployee);
		return new ErrorResponse(id);
	}
	
	@RequestMapping("/delete_emp")
	@PreAuthorize("hasRole('DELETE_EMP')")
	public ErrorResponse delete_emp(@RequestParam(value="id", defaultValue="-1") int id)
	{
		if(dbDAO.deleteEmployee(id))
			return new ErrorResponse("ok");
		else
			return new ErrorResponse("No records deleted");
	}
	
	@RequestMapping(value = "/update_emp", method = RequestMethod.POST)	
	@PreAuthorize("hasRole('UPDATE_EMP')")
	public ErrorResponse update_emp(@RequestBody Employee updatedEmployee)
	{
		if(dbDAO.updateEmployee(updatedEmployee))
			return new ErrorResponse("ok");
		else
			return new ErrorResponse("No records updated");
	}
	
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public LoginResponse login(@RequestBody Login login)
	{
		LoginResponse resp = new LoginResponse();
		Employee e = dbDAO.getEmployeeByUsername(login.username, login.password);
		if(e == null)
		{
			resp.error = "Unable to validate user/password combination";
		}
		else
		{
			JwtToken token = new JwtToken(e);
			try {
				resp.jwt = token.getToken();
			} catch (Exception e1) {
				e1.printStackTrace();
				resp.jwt = null;
				resp.error = e1.getMessage();
			}
		}
		return resp;
	}
}
