package com.punchin.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
public class AgentDocTypeDTO {

    List<HashMap<String, String>> names;
}
