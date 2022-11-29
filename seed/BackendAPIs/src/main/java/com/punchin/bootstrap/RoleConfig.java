package com.punchin.bootstrap;

import com.punchin.enums.DefaultRoles;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class RoleConfig {
	private JSONArray roles = new JSONArray();
	public static final String ID ="id";
	public static final String REPRESENTATION = "representationName";
	public static final String DESCRIPTION = "description";
	public static final String NAME ="name";
	public RoleConfig()  {
		
		JSONObject roleUser = new JSONObject();

		JSONObject roleSuperAdmin = new JSONObject();
		roleSuperAdmin.put(RoleConfig.REPRESENTATION,"SUPER_ADMIN");
		roleSuperAdmin.put(RoleConfig.NAME, DefaultRoles.SUPER_ADMIN);
		roleSuperAdmin.put(RoleConfig.DESCRIPTION,"The user with this role have super Admin rights");
		this.roles.put(roleSuperAdmin);

		JSONObject roleAdmin = new JSONObject();
		roleAdmin.put(RoleConfig.REPRESENTATION,"ADMIN");
		roleAdmin.put(RoleConfig.NAME,DefaultRoles.ROLE_ADMIN);
		roleAdmin.put(RoleConfig.DESCRIPTION,"The user with this role have all the rights of admin");
		this.roles.put(roleAdmin);

		JSONObject roleBanker = new JSONObject();
		roleBanker.put(RoleConfig.REPRESENTATION,"BANKER");
		roleBanker.put(RoleConfig.NAME,DefaultRoles.ROLE_BANKER);
		roleBanker.put(RoleConfig.DESCRIPTION,"The user with this role have all the rights of Banker");
		this.roles.put(roleBanker);

		JSONObject roleVerifier = new JSONObject();
		roleVerifier.put(RoleConfig.REPRESENTATION,"VERIFIER");
		roleVerifier.put(RoleConfig.NAME,DefaultRoles.ROLE_VERIFIER);
		roleVerifier.put(RoleConfig.DESCRIPTION,"The user with this role have all the rights of verifier");
		this.roles.put(roleVerifier);

		roleUser.put(RoleConfig.REPRESENTATION,"USER");
		roleUser.put(RoleConfig.NAME,DefaultRoles.ROLE_AGENT);
		roleUser.put(RoleConfig.DESCRIPTION,"The user with this role have all the rights of agent");
		this.roles.put(roleUser);

	}
	
	
	public JSONArray getRoles() {
		return this.roles;
	}
}
