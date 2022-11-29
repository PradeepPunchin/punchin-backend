package com.punchin.utility;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.LongType;
import org.hibernate.type.Type;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Properties;

@Component
public class UserSequenceIdGenerator extends SequenceStyleGenerator {

	public static final String VALUE_PREFIX_PARAMETER = "prefix";
	public static final String VALUE_PREFIX_DEFAULT = "";
	private String prefix;

	public static final String NUMBER_FORMAT_PARAMETER = "numberFormat";
	public static final String NUMBER_FORMAT_DEFAULT = "%d";
	

	@Override
	public Serializable generate(SharedSessionContractImplementor session, Object object) {
		Calendar cal = Calendar.getInstance();
		int day = cal.get(Calendar.DAY_OF_MONTH) * 10;
		int month = cal.get(Calendar.MONTH) + 1;
		int year = cal.get(Calendar.YEAR);
		prefix = String.valueOf(year) + String.valueOf(month) + String.valueOf(day);
		Long seq = (Long) super.generate(session, object);
		return Long.valueOf(prefix) + seq;
	}

	@Override
	public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) {
		super.configure(LongType.INSTANCE, params, serviceRegistry);
		prefix = ConfigurationHelper.getString(VALUE_PREFIX_PARAMETER, params, VALUE_PREFIX_DEFAULT);
	}

}
