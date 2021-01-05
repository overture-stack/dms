/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package bio.overture.dms.rest;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

@Slf4j
@RequiredArgsConstructor
public class RetryingRestClientDecorator implements RestClient {

  @NonNull private final RestClient internalRestClient;
  @NonNull private final RetryPolicy<String> retryPolicy;

  @Override
  public String getString(String endpoint) {
    return tryRequest(() -> internalRestClient.getString(endpoint));
  }

  @Override
  public String postString(String endpoint, Object body) {
    return tryRequest(() -> internalRestClient.postString(endpoint, body));
  }

  @Override
  public String putString(String endpoint, Object body) {
    return tryRequest(() -> internalRestClient.putString(endpoint, body));
  }

  private String tryRequest(Supplier<String> supplier) {
    return Failsafe.with(retryPolicy).get(supplier::get);
  }
}
