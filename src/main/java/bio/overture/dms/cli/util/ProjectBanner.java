/*
 * Copyright (c) 2020. Ontario Institute for Cancer Research
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
 *
 */

package bio.overture.dms.cli.util;

import static com.github.lalyos.jfiglet.FigletFont.convertOneLine;
import static java.util.Arrays.stream;

import java.io.PrintStream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.boot.Banner;
import org.springframework.boot.ResourceBanner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;

@RequiredArgsConstructor
public class ProjectBanner implements Banner {

  /** Other fonts can be found at http://www.figlet.org/examples.html */
  private static final String BANNER_FONT_LOC = "/banner-fonts/slant.flf";

  @NonNull private final String applicationName;
  // Example: "${Ansi.GREEN} "
  @NonNull private final String linePrefix;
  @NonNull private final String linePostfix;

  @Override
  @SneakyThrows
  public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
    val text = generateBannerText();
    val resource = new ByteArrayResource(text.getBytes());
    val resourceBanner = new ResourceBanner(resource);
    resourceBanner.printBanner(environment, sourceClass, out);
  }

  @SneakyThrows
  public String generateBannerText() {
    val text = convertOneLine("classpath:" + BANNER_FONT_LOC, applicationName);
    val sb = new StringBuilder();
    stream(text.split("\n"))
        .forEach(t -> sb.append(linePrefix).append(t).append(linePostfix).append("\n"));
    return sb.toString();
  }
}
