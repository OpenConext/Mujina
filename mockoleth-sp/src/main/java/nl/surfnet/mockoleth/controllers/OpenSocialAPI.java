/*
 * Copyright 2012 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.surfnet.mockoleth.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class OpenSocialAPI {

  private final static Logger log = LoggerFactory.getLogger(OpenSocialAPI.class);

  @RequestMapping(value = { "/social-queries.do" }, method = RequestMethod.GET)
  public String socialQueries(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    modelMap.addAttribute("requestInfo",
        IOUtils.toString(new ClassPathResource("multiple-persons.json").getInputStream()));
    modelMap.addAttribute("responseInfo", response.toString());
    return "social-queries";
  }

}
