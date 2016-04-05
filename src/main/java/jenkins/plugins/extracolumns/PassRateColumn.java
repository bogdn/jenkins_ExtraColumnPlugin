/*
 * The MIT License
 *
 * Copyright (c) 2013, Frederic Gurr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package jenkins.plugins.extracolumns;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;
import org.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;

import java.nio.file.Files;
import java.nio.file.Paths;

import static java.io.File.separator;

public class PassRateColumn extends ListViewColumn {

    @DataBoundConstructor
    public PassRateColumn() {
        super();
    }

    @Extension
    public static class DescriptorImpl extends ListViewColumnDescriptor {

        @Override
        public boolean shownByDefault() {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.PassRateColumn_DisplayName();
        }
    }

    public String getPassRate(Job job) {
        String jsonString = null;
        JSONObject jsonObj = null;
        String filePath = null;
        String passRate = null;

        try {
            if (!job.getIconColor().isAnimated()) {
                filePath = job.getLastBuild().getRootDir().getAbsolutePath() +
                        separator + "htmlreports" + separator + "Report" + separator + "report.json";
            } else {
                Run lastNotBuildingNowJob = job.getBuilds().getLastBuild().getPreviousBuiltBuild();
                filePath = lastNotBuildingNowJob.getRootDir().getAbsolutePath() +
                        separator + "htmlreports" + separator + "Report" + separator + "report.json";
            }

            jsonString = new String(Files.readAllBytes(Paths.get(filePath)));

            jsonObj = new JSONObject(jsonString);
            passRate = jsonObj.getJSONObject("suiteData").get("suitePassRate") + "%";

        } catch (Exception e) {
            return "N/A";
        }
        return passRate;
    }

    public String getLastReportHtmlPath(Job job) {
        return job.getAbsoluteUrl() + "Report" + "/" + "report.html";
    }
}
