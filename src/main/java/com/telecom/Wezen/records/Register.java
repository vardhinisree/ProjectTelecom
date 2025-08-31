package com.telecom.Wezen.records;

import com.telecom.Wezen.enums.Role;

public record Register(
		 String name,
	    String mail,
	    String password,
	    String phoneNo,
	    Role role 
	    
) {

	

}
