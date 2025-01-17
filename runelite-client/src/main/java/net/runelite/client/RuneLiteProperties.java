/*
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.RuneLightConfig;

@Singleton
@Slf4j
public class RuneLiteProperties
{
	private static final String RUNELITE_TITLE = "runelite.plus.title";
	private static final String RUNELITE_VERSION = "runelite.version";

	private static final Properties properties = new Properties();

	private final RuneLightConfig runeLightConfig;

	@Inject
	public RuneLiteProperties(final RuneLightConfig runeLightConfig)
	{
		this.runeLightConfig = runeLightConfig;

		try (InputStream in = getClass().getResourceAsStream("/runelite.plus.properties"))
		{
			properties.load(in);
		}
		catch (IOException ex)
		{
			log.warn("unable to load propertries", ex);
		}
	}

	public RuneLiteProperties()
	{
		runeLightConfig = null;
	}

	public static String getTitle()
	{
		final StringBuilder sb = new StringBuilder(properties.getProperty(RUNELITE_TITLE));
		String proxy;
		if ((proxy = System.getProperty("socksProxyHost")) != null)
		{
			sb.append(String.format(" (%s)", proxy));
		}
		return sb.toString();
	}

	public String getVersion()
	{
		return properties.getProperty(RUNELITE_VERSION);
	}
}
