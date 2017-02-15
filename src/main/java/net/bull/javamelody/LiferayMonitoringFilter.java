/*
 * Copyright 2008-2016 by Emeric Vernat
 *
 *     This file is part of Java Melody.
 *
 * Java Melody is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Java Melody is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Java Melody.  If not, see <http://www.gnu.org/licenses/>.
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
			System.setProperty(Parameters.PARAMETER_SYSTEM_PREFIX + Parameter.SQL_TRANSFORM_PATTERN.getCode(), "\\([\\?, ]+\\)");
		}

		if (Parameters.getParameter(Parameter.DISPLAYED_COUNTERS) == null) {
			// disable jsp counter to fix
			// https://github.com/javamelody/liferay-javamelody/issues/5,
			// the jsp counter does not display anything anyway.
			// In consequence, jsf, job, ejb, jpa, spring, guice are also
			// disabled.
			System.setProperty(Parameters.PARAMETER_SYSTEM_PREFIX + Parameter.DISPLAYED_COUNTERS.getCode(), "http,sql,error,log");
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
		if (httpRequest.getRequestURI().equals(getMonitoringUrl(httpRequest))) {
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
		final long userId = PortalUtil.getUserId(httpRequest);
		final long companyId = PortalUtil.getDefaultCompanyId();
		return UserLocalServiceUtil.hasRoleUser(companyId, RoleConstants.ADMINISTRATOR, userId, true);
	}
}
