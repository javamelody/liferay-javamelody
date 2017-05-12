/*
 * Copyright 2008-2017 by Emeric Vernat
 *
 *     This file is part of the Liferay JavaMelody plugin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.bull.javamelody;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.PortalUtil;

/**
 * Filter of monitoring JavaMelody for Liferay.
 * 
 * @author Emeric Vernat
 */
public class LiferayMonitoringFilter extends PluginMonitoringFilter {
	private static final boolean PLUGIN_AUTHENTICATION_DISABLED = Boolean
			.parseBoolean(System.getProperty("javamelody.plugin-authentication-disabled"));

	private boolean liferay7;

	/** {@inheritDoc} */
	@Override
	public String getApplicationType() {
		return "Liferay";
	}

	/** {@inheritDoc} */
	@Override
	public void init(FilterConfig config) throws ServletException {
		// rewrap datasources in GlobalNamingResources with ResourceLink in
		// context.xml
		System.setProperty(Parameters.PARAMETER_SYSTEM_PREFIX + Parameter.REWRAP_DATASOURCES.getCode(),
				Boolean.TRUE.toString());

		if (Parameters.getParameter(Parameter.SQL_TRANSFORM_PATTERN) == null) {
			// regexp pour agréger les paramètres bindés dans les critères
			// de requêtes SQL tels que "in (?, ?, ?, ?)" et ainsi pour éviter
			// que ces requêtes ayant un nombre variable de paramètres soient
			// considérées comme différentes ;
			// de fait cela agrège aussi les values des inserts
			System.setProperty(Parameters.PARAMETER_SYSTEM_PREFIX + Parameter.SQL_TRANSFORM_PATTERN.getCode(),
					"\\([\\?, ]+\\)");
		}

		if (Parameters.getParameter(Parameter.DISPLAYED_COUNTERS) == null) {
			// disable jsp counter to fix
			// https://github.com/javamelody/liferay-javamelody/issues/5,
			// the jsp counter does not display anything anyway.
			// In consequence, jsf, job, ejb, jpa, spring, guice are also
			// disabled.
			System.setProperty(Parameters.PARAMETER_SYSTEM_PREFIX + Parameter.DISPLAYED_COUNTERS.getCode(),
					"http,sql,error,log");
		}

		super.init(config);

		LOG.debug("JavaMelody is monitoring Liferay");
	}

	/** {@inheritDoc} */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (!(request instanceof HttpServletRequest)) {
			super.doFilter(request, response, chain);
			return;
		}
		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpServletResponse httpResponse = (HttpServletResponse) response;
		if (!PLUGIN_AUTHENTICATION_DISABLED && httpRequest.getRequestURI().equals(getMonitoringUrl(httpRequest))) {
			if (isRumMonitoring(httpRequest, httpResponse)) {
				return;
			}
			try {
				if (!isAdmin(httpRequest)) {
					LOG.debug("Forbidden access to monitoring from " + request.getRemoteAddr());
					httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden access");
					httpResponse.flushBuffer();
					return;
				}
			} catch (final Exception e) {
				throw new ServletException(e);
			}
		}

		super.doFilter(request, response, chain);
	}

	private boolean isAdmin(HttpServletRequest httpRequest) throws PortalException, SystemException {
		try {
			if (!liferay7) {
				// for liferay 6
				final long userId = com.liferay.portal.util.PortalUtil.getUserId(httpRequest);
				final long companyId = com.liferay.portal.util.PortalUtil.getDefaultCompanyId();
				return com.liferay.portal.service.UserLocalServiceUtil.hasRoleUser(companyId,
						com.liferay.portal.model.RoleConstants.ADMINISTRATOR, userId, true);
			}
		} catch (final NoClassDefFoundError e) {
			liferay7 = true;
		}
		if (liferay7) {
			// for liferay 7
			final long userId = PortalUtil.getUserId(httpRequest);
			final long companyId = PortalUtil.getDefaultCompanyId();
			return UserLocalServiceUtil.hasRoleUser(companyId, RoleConstants.ADMINISTRATOR, userId, true);
		}
		return false;
	}
}
