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
import hudson.util.RunList;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;
import org.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.io.File.separator;

public class TodaysBestPassRateColumn extends ListViewColumn {

    public String reportHtmlPath;

    @DataBoundConstructor
    public TodaysBestPassRateColumn() {
        super();
    }

    @Extension
    public static class DescriptorImpl extends ListViewColumnDescriptor {

        @Override
        public boolean shownByDefault() {
            return false;
        }

        @Override
        public String getDisplayName() {
            return Messages.TodaysBestPassRateColumn_DisplayName();
        }
    }

    public String getTodaysBestPassRate(Job job) {

        try {
            return getBestPassRate(getTodaysRuns(job));
        } catch (Exception e) {
            return "0%";
        }
    }

    public List<Run> getTodaysRuns(Job job) throws Exception {
        List<Run> todaysRuns = new ArrayList<Run>();
        RunList allRuns = job.getBuilds();
        for (int i = 0; i < allRuns.size(); i++) {
            Run run = (Run) allRuns.get(i);
            if (isTodayDate(run.getTime())) {
                todaysRuns.add(run);
            }
        }
        return todaysRuns;
    }

    public String getBestPassRate(List<Run> runs) {
        String bestRunPassRate = "0";

        try {
            for (Run run : runs) {
                if (Float.parseFloat(getPassRateFromRun(run)) > Float.parseFloat(bestRunPassRate)) {
                    bestRunPassRate = getPassRateFromRun(run);
                }
            }
        } catch (Exception e) {
            return "0%";
        }

        return bestRunPassRate + "%";
    }

    public String getPassRateFromRun(Run run) {
        String jsonPath = null;
        String passRate = "0";
        String jsonString = null;
        JSONObject jsonObj = null;
        try {
            jsonPath = run.getRootDir().getAbsolutePath() +
                    separator + "htmlreports" + separator + "Report" + separator + "report.json";
            jsonString = new String(Files.readAllBytes(Paths.get(jsonPath)));
            jsonObj = new JSONObject(jsonString);
            passRate = jsonObj.getJSONObject("suiteData").get("suitePassRate").toString();
        } catch (Exception e) {
            return "0";
        }
        return passRate;
    }

    public boolean isTodayDate(Date runDate) {
        Date currentDate = new Date();
        LocalDate localRunDate = runDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localCurrentDate = currentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate increasedRunDate = localRunDate.plusDays(2);
        return increasedRunDate.isAfter(localCurrentDate);
    }
}
