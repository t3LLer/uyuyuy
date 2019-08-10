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
package net.runelite.http.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import java.util.concurrent.TimeUnit;

public class RuneLiteAPI {
	private static final String version = "1.5.30.1";
	private static final String serviceVersion = "1.5.31-SNAPSHOT";
	private static final int rsVersion = 181;

	public static final OkHttpClient CLIENT;
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static final String BASE = "http://localhost:8080/";
	private static final String STATICBASE = "https://raw.githubusercontent.com/runelite/static.runelite.net/gh-pages/";
	public static final String RUNELITE_PRICES = "https://api.runelite.net/runelite-" + getVersion() + "/item/prices.js";

	static {

		CLIENT = new OkHttpClient.Builder()
				.connectTimeout(8655, TimeUnit.MILLISECONDS)
				.writeTimeout(8655, TimeUnit.MILLISECONDS)
				.addNetworkInterceptor(chain -> {
					Request userAgentRequest = chain.request()
							.newBuilder()
							.header("User-Agent", "RuneLite/" + version + "-" + "169eb5751dc8d98c602f4cf03a26d6ac4d8995db")
							.build();
					return chain.proceed(userAgentRequest);
				})
				.build();
	}

	public static HttpUrl getApiBase() {
		return HttpUrl.parse(BASE + "http-service-"+ serviceVersion);
	}

	public static HttpUrl getStaticBase() {
		return HttpUrl.parse(STATICBASE);
	}

	public static String getVersion() {
		return version;
	}

	public static int getRsVersion() {
		return rsVersion;
	}
}
