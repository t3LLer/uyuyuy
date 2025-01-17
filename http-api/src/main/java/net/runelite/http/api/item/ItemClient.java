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
package net.runelite.http.api.item;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import io.reactivex.Observable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import net.runelite.http.api.RuneLiteAPI;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.runelite.http.api.RuneLiteAPI.RUNELITE_PRICES;

public class ItemClient
{
	private static final Logger logger = LoggerFactory.getLogger(ItemClient.class);

	public Observable<Map<Integer, ItemStats>> getStats()
	{
		HttpUrl.Builder urlBuilder = RuneLiteAPI.getStaticBaseGithub().newBuilder()
			.addPathSegment("item")
			.addPathSegment("stats.ids.min.json");

		HttpUrl url = urlBuilder.build();

		logger.debug("Built URI: {}", url);


		return Observable.defer(() ->
		{
			Request request = new Request.Builder()
				.url(url)
				.build();

			try (Response response = RuneLiteAPI.CLIENT.newCall(request).execute())
			{
				if (!response.isSuccessful())
				{
					logger.warn("Error looking up item stats: {}", response);
					return Observable.error(new IOException("Error looking up item stats: " + response));
				}

				InputStream in = Objects.requireNonNull(response.body()).byteStream();
				final Type typeToken = new TypeToken<Map<Integer, ItemStats>>()
				{
				}.getType();
				return Observable.just(RuneLiteAPI.GSON.fromJson(new InputStreamReader(in), typeToken));
			}
			catch (JsonParseException ex)
			{
				return Observable.error(ex);
			}
		});
	}

	public Observable<ItemPrice[]> getPrices()
	{
		return Observable.defer(() ->
		{
			Request request = new Request.Builder()
					.url(RUNELITE_PRICES)
					.build();

			try (Response response = RuneLiteAPI.CLIENT.newCall(request).execute())
			{
				if (!response.isSuccessful())
				{
					logger.warn("Error looking up prices: {}", response);
					return Observable.error(new IOException("Error looking up prices: " + response));
				}

				logger.info("Loaded RuneLite Prices: {}", response);
				InputStream in = Objects.requireNonNull(response.body()).byteStream();
				return Observable.just(RuneLiteAPI.GSON.fromJson(new InputStreamReader(in), ItemPrice[].class));
			}
			catch (JsonParseException ex)
			{
				return Observable.error(ex);
			}
		});
	}
}
