package org.openmrs.performance.registries;

import io.gatling.javaapi.core.ChainBuilder;
import org.openmrs.performance.http.FormBuilderHttpService;

import static io.gatling.javaapi.core.CoreDsl.exec;

public class FormBuilderRegistry extends Registry<FormBuilderHttpService> {

	public FormBuilderRegistry() {
		super(new FormBuilderHttpService());
	}

	public ChainBuilder openFormBuilderTab() {
		return exec(httpService.getFrontendConfig(), httpService.getSessionInfo(), httpService.getModuleInfo(),
		    httpService.getAllForms());
	}
}
