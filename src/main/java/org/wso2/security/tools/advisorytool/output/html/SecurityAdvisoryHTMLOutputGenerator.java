/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.security.tools.advisorytool.output.html;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.Logger;
import org.wso2.security.tools.advisorytool.exeption.AdvisoryToolException;
import org.wso2.security.tools.advisorytool.model.SecurityAdvisory;
import org.wso2.security.tools.advisorytool.output.SecurityAdvisoryOutputGenerator;
import org.wso2.security.tools.advisorytool.utils.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * This class generates the security advisory HTML.
 */
public class SecurityAdvisoryHTMLOutputGenerator implements SecurityAdvisoryOutputGenerator {
    private static final Logger logger = Logger.getLogger(SecurityAdvisoryHTMLOutputGenerator.class);

    @Override
    public boolean isAdvisoryGenerateFromFile() {
        return false;
    }

    @Override
    public void generate(SecurityAdvisory securityAdvisory) throws AdvisoryToolException {
        File outputFile = new File(Constants.SECURITY_ADVISORY_OUTPUT_DIRECTORY + File.separator
                + "html" + File.separator + securityAdvisory.getName() + ".html");

        File outputDirectory = new File(outputFile.getParent());
        outputDirectory.mkdirs();
        if (!outputDirectory.exists()) {
            throw new AdvisoryToolException("Unable to create the directory " + outputDirectory);
        }

        logger.info("Security Advisory HTML generation started");
        try (PrintWriter pw = new PrintWriter(outputFile, "UTF-8")) {

            pw.write(generateAdvisoryHTML(securityAdvisory));
            logger.info("Security Advisory HTML generation completed");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new AdvisoryToolException("Failed to generate the Security Advisory HTML.", e);
        }
    }

    /**
     * Generating the html string for a given security advisory.
     * @param securityAdvisory
     * @return
     * @throws AdvisoryToolException
     */
    protected String generateAdvisoryHTML(SecurityAdvisory securityAdvisory) throws AdvisoryToolException {
        TypeToken<Map<String, Object>> typeToken = new TypeToken<Map<String, Object>>() {
        };
        Gson gson = new Gson();
        String htmlString;

        try {
            TemplateLoader loader = new ClassPathTemplateLoader();
            loader.setPrefix(Constants.SECURITY_ADVISORY_HTML_TEMPLATE_DIRECTORY);
            loader.setSuffix(".hbs");
            Handlebars handlebars = new Handlebars(loader);
            Template template = handlebars.compile(Constants.SECURITY_ADVISORY_HTML_TEMPLATE);

            String jsonString = gson.toJson(securityAdvisory);
            Map<String, Object> pdfInfoMap = gson.fromJson(jsonString, typeToken.getType());

            Context context = Context.newBuilder(pdfInfoMap).build();
            htmlString = template.apply(context);
        } catch (IOException e) {
            throw new AdvisoryToolException("Failed to generate the Security Advisory HTML.", e);
        }
        return htmlString;
    }
}
