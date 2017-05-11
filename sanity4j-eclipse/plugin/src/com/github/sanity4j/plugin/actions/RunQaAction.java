package com.github.sanity4j.plugin.actions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;

import com.github.sanity4j.plugin.Activator;
import com.github.sanity4j.plugin.util.FileUtil;
import com.github.sanity4j.plugin.views.SimpleBrowserView;
import com.github.sanity4j.util.QAException;
import com.github.sanity4j.workflow.QAConfig;
import com.github.sanity4j.workflow.QAProcessor;
import com.github.sanity4j.workflow.WorkUnit;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.dialogs.InternalErrorDialog;
import org.eclipse.ui.part.FileEditorInput;

/**
 * RunQaAction - an eclipse action to run the Sanity4J task.
 *
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class RunQaAction implements IEditorActionDelegate
{
    /** A reference to the active editor. */
    private IEditorPart editor;

    /**
     * Sets the active editor.
     *
     * @param action the action proxy that handles presentation portion of the action
     * @param targetEditor the new editor
     */
    public void setActiveEditor(final IAction action, final IEditorPart targetEditor)
    {
        editor = targetEditor;
    }

    /**
     * Run the action.
     *
     * @param proxyAction the action proxy that handles the presentation portion of the action
     */
    public void run(final IAction proxyAction)
    {
        IJavaElement javaElement = JavaUI.getEditorInputJavaElement(editor.getEditorInput());

        if (javaElement == null)
        {
            alert("QA can only be run on files that are part of a java project.");
        }
        else
        {
            // Save file before running QA
            if (editor.isDirty())
            {
                IWorkbenchPage page = editor.getSite().getPage();
                page.saveEditor(editor, false);
            }

            String outputDir = getProjectOutputLocation().toOSString();
            IPath projectSourceLocation = getProjectSourceLocation();

            if (projectSourceLocation != null)
            {
                String targetClass = getTargetClassName();
                runQa(targetClass, projectSourceLocation.toOSString(), outputDir);
            }
        }
    }

    /**
     * Creates a temporary directory on the file system, to be used by the QA task.
     *
     * @return the temporary directory File, or null if there was an error.
     */
    private File createTempDir()
    {
        File tempFile = null;

        try
        {
            tempFile = new File(System.getProperty("java.io.tmpdir"), "sanity4jPlugin");
            FileUtil.delete(tempFile);

            if (!tempFile.mkdir())
            {
                throw new IOException("Mkdir returned false");
            }
        }
        catch (IOException e)
        {
            tempFile = null;
            alert("Failed to create temporary directory", e);
        }

        return tempFile;
    }

    /**
     * Creates a Runnable version of the Plugin QA Ant task.
     *
     * @param runQA the QA task
     * @return a runnable that can be monitored by the eclispse UI
     */
    private IRunnableWithProgress getRunQARunnable(final PluginQAProcessor runQA)
    {
        return new IRunnableWithProgress()
        {
            public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
            {
                monitor.setTaskName("Sanity4J task");
                monitor.beginTask("Analysis", /* 1 */IProgressMonitor.UNKNOWN);

                try
                {
                    runQA.setProgressMonitor(monitor);
                    runQA.run();
                    // monitor.subTask("xxx");
                    // monitor.worked(1);
                }
                finally
                {
                    monitor.done();
                }
            }
        };
    }

    /**
     * Runs QA on the given class.
     *
     * @param className the fully qualified name of the class to analyse
     * @param sourceDir the source directory for the given class
     * @param classDir the class directory for the given class
     */
    private void runQa(final String className, final String sourceDir, final String classDir)
    {
        File reportDir = createTempDir();

        if (reportDir == null)
        {
            // Failed to clean out temp dir
            return;
        }

        // Ant uses '/' as the path separator regardless of OS
        String sourceFilePath = sourceDir + '/' + className.replace('.', '/') + ".java";
        String classFilePath = classDir + '/' + className.replace('.', '/') + ".class"; // TODO: Inner classes?

        if (!new File(sourceFilePath.replace('/', File.separatorChar)).exists())
        {
            alert("Can not find source file for " + className + ".\n Expected to find source at "
                  + sourceFilePath.replace('/', File.separatorChar));

            return;
        }

        if (!new File(classFilePath.replace('/', File.separatorChar)).exists())
        {
            alert("Can not find class file for " + className + ". \n Expected to find class at "
                  + classFilePath.replace('/', File.separatorChar) + " . \nCheck 'Problems' view for build errors.");

            return;
        }

        boolean runSucceed = false;

        PluginQAProcessor runQA = new PluginQAProcessor();
        runQA.getConfig().setJavaRuntime(Activator.getJavaRuntime());
        runQA.getConfig().setProductsDir(Activator.getProductsDir());
        runQA.getConfig().setExternalPropertiesPath(Activator.getSanity4JProperties());
        runQA.getConfig().setDiagnosticsFirst(Activator.getDiagnosticsFirst());
        runQA.getConfig().addSourcePath(sourceFilePath);
        runQA.getConfig().addClassPath(classFilePath);
        runQA.getConfig().setReportDir(reportDir.getPath());

        for (String tool : runQA.getConfig().getToolsToRun())
        {
            QAConfig config = runQA.getConfig();
            String version = config.getToolVersion(tool);

            String toolConfig = Activator.getToolConfig(tool, version);
            String toolConfigClasspath = Activator.getToolConfigClasspath(tool, version);

            if (FileUtil.hasValue(toolConfig))
            {
                config.setToolConfig(tool, null, toolConfig, toolConfigClasspath);
                config.setToolConfig(tool, version, toolConfig, toolConfigClasspath);
            }
        }

        ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(editor.getEditorSite().getShell());

        try
        {
            progressDialog.run(true, false, getRunQARunnable(runQA));
            runSucceed = true;
        }
        catch (Exception e)
        {
            // Cancelled ?

            if (e.getCause() instanceof QAException)
            {
                Activator.getDefault().getLog()
                    .log(new Status(Status.ERROR, "com.github.sanity4j.plugin", Status.OK, "Sanity4J failed", e));
                alert("Sanity4J failed", e);
            }
            else if (e instanceof InvocationTargetException)
            {
                Activator.getDefault().getLog()
                    .log(new Status(Status.ERROR, "com.github.sanity4j.plugin", Status.OK, "Sanity4J failed", e));
                alert("Unknown error running Sanity4J",  e.getCause());
            }
            else
            {
                Activator.getDefault().getLog()
                    .log(new Status(Status.ERROR, "com.github.sanity4j.plugin", Status.OK, "Sanity4J failed", e));
                alert("Unknown error running Sanity4J", e);
            }
        }

        if (runSucceed)
        {
            // Resultant file should be reportDir/packageName/className.html
            File file = new File(reportDir, className.replace('.', File.separatorChar) + ".html");

            if (file.exists())
            {
                try
                {
                    openBrowser(file.toURI().toURL());
                }
                catch (Exception e)
                {
                    alert("Failed to open QA result file [" + file + "]", e);
                }
            }
            else
            {
                file = new File(reportDir, className.replace('.', File.separatorChar) + ".xml");

                if (file.exists())
                {
                    try
                    {
                        openBrowser(file.toURI().toURL());
                    }
                    catch (Exception e)
                    {
                        alert("Failed to open QA result file [" + file + "]", e);
                    }
                }

            }
        }
    }

    /** The plug-in view. Must match the id in the plugin.xml. */
    public static final String PLUGIN_VIEW = "com.github.sanity4j.eclipse.plugin.views.SimpleBrowserView";

    /**
     * Opens a browser window.
     *
     * @param url the url to open
     */
    private void openBrowser(final URL url)
    {
        IWorkbenchPage[] pages = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages();
        IWorkbenchPage page = pages[0];

        SimpleBrowserView browser = (SimpleBrowserView) page.findView(PLUGIN_VIEW);

        if (browser == null)
        {
            try
            {
                browser = (SimpleBrowserView) page.showView(PLUGIN_VIEW);
            }
            catch (PartInitException e)
            {
                alert("Error initialising browser view", e);
            }
        }

        if (browser != null)
        {
            browser.navigateTo(url.toString());
            page.bringToTop(browser);
        }
    }

    /**
     * Presents an alert dialog to the user.
     *
     * @param message the message to display
     */
    private void alert(final String message)
    {
        Shell shell = editor.getEditorSite().getShell();
        MessageDialog.openInformation(shell, "Eclipse Sanity4J plugin", message);
    }

    /**
     * Presents an alert dialog to the user.
     *
     * @param message the message to display
     * @param detail the exception which caused the error.
     */
    private void alert(final String message, Throwable detail)
    {
        Shell shell = editor.getEditorSite().getShell();
        InternalErrorDialog.openQuestion(shell, "Eclipse Sanity4J plugin", message, detail, 0);
    }

    /**
     * Determines the target class name, based on the active editor's content.
     *
     * @return the fully qualified name of the target class
     */
    private String getTargetClassName()
    {
        String javaSourcePath = getActiveFile().toString();
        String projectSourcePath = getProjectSourceLocation().toString();

        String sourceName = javaSourcePath.substring(projectSourcePath.length() + 1); // trailing slash
        String className = sourceName.substring(0, sourceName.toLowerCase().indexOf(".java")).replace('/', '.');

        return className;
    }

    /**
     * @return the file currently being edited, or null if there isn't one.
     */
    private IPath getActiveFile()
    {
        if (editor != null)
        {
            IEditorInput input = editor.getEditorInput();

            if (input instanceof FileEditorInput)
            {
                return ((FileEditorInput) input).getPath();
            }
        }

        return null;
    }

    /**
     * @return the source directory of the current editor's project.
     */
    private IPath getProjectSourceLocation()
    {
        try
        {
            IJavaElement javaElement = JavaUI.getEditorInputJavaElement(editor.getEditorInput());
            IJavaProject project = javaElement.getJavaProject();
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            IClasspathEntry[] classPath = project.getRawClasspath();
            String relativeSourcePath = javaElement.getPath().toString();

            for (int i = 0; i < classPath.length; i++)
            {
                if (classPath[i].getEntryKind() == IClasspathEntry.CPE_SOURCE)
                {
                    String projectSourcePath = classPath[i].getPath().toString();

                    if (relativeSourcePath.startsWith(projectSourcePath))
                    {
                        IFolder folder = root.getFolder(classPath[i].getPath());
                        return folder.getRawLocation();
                    }
                }
            }
        }
        catch (JavaModelException e)
        {
            alert("Unable to determine project source location", e);
        }

        alert("Unable to determine project source location");

        return null;
    }

    /**
     * @return the output directory of the current editor's project.
     */
    private IPath getProjectOutputLocation()
    {
        try
        {
            IJavaElement javaElement = JavaUI.getEditorInputJavaElement(editor.getEditorInput());
            IJavaProject project = javaElement.getJavaProject();
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

            IFolder folder = root.getFolder(project.getOutputLocation());
            return folder.getRawLocation();
        }
        catch (JavaModelException e)
        {
            return null;
        }
    }

    /**
     * Called when the selection in the workbench has changed. This implementation does nothing, as the plugin is not
     * dependant on selection.
     *
     * @param proxyAction the action proxy that handles the presentation portion of the action
     * @param selection the new selection
     */
    public void selectionChanged(final IAction proxyAction, final ISelection selection)
    {
        // do nothing, action is not dependent on the selection
    }

    /**
     * An extension of the RunQA task, so that progress can be monitored in the eclipse UI.
     */
    private static final class PluginQAProcessor extends QAProcessor
    {
        /** The eclipse progress monitor. */
        private IProgressMonitor monitor;

        /**
         * Sets the progress monitor.
         *
         * @param monitor the monitor to set
         */
        public void setProgressMonitor(final IProgressMonitor monitor)
        {
            this.monitor = monitor;
        }

        /**
         * Override runWork so that we can update the progress monitor.
         *
         * @param workUnits the units of work to run.
         */
        protected void runWork(final List<WorkUnit> workUnits)
        {
            for (int i = 0; i < workUnits.size(); i++)
            {
                WorkUnit work = (WorkUnit) workUnits.get(i);

                if (monitor != null)
                {
                    monitor.subTask(work.getDescription());
                }

                work.run();
            }
        }
    }
}
