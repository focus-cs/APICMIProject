/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cmi.sciforma;

import com.cmi.sciforma.beans.Connector;
import com.cmi.sciforma.beans.SciformaField;
import com.sciforma.psnext.api.CostAssignment;
import com.sciforma.psnext.api.DataFormatException;
import com.sciforma.psnext.api.Global;
import com.sciforma.psnext.api.InvalidDataException;
import com.sciforma.psnext.api.PSException;
import com.sciforma.psnext.api.Project;
import com.sciforma.psnext.api.ResAssignment;
import com.sciforma.psnext.api.Session;
import com.sciforma.psnext.api.Task;
import com.sciforma.psnext.api.TaskOutlineList;
import com.sciforma.psnext.api.TaskStep;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.pmw.tinylog.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 *
 * @author Eric
 */
public class Run {

    public static ApplicationContext ctx;

    private static String IP;
    private static String PORT;
    private static String CONTEXTE;
    private static String USER;
    private static String PWD;

    public static Session mSession;

    private static List<Project> projectList = null;
    private static Date today;
    private static SimpleDateFormat sdf;

    public static String APPLICATION_HEURE;

    public static String APPLICATION_WBS;

    public static String APPLICATION_RENUMEROTATION;

    public static String APPLICATION_CAPTURE_BUDGETAIRE;

    public static String APPLICATION_DECREMENTATION;
    public static String APPLICATION_DECREMENTATION_DEFAULT_LOGIN;//Administrator, Production
    public static String APPLICATION_DECREMENTATION_SCRIPTMAILCC;//scriptMailcc
    public static String APPLICATION_DECREMENTATION_SCRIPTMAIL;//scriptMail
    public static String APPLICATION_DECREMENTATION_SCRIPTMAILDEBUT;//scriptMaildebut
    public static String APPLICATION_DECREMENTATION_DELTARW;//Delta Rolling Wave
    public static String APPLICATION_DECREMENTATION_CAPTURERW;//Capture Rolling Wave
    public static String APPLICATION_DECREMENTATION_JOB;//Métier direct
    public static String APPLICATION_DECREMENTATION_LOT;//Lot de Travaux
    public static String APPLICATION_DECREMENTATION_NATURE;//Nature taches
    public static String APPLICATION_DECREMENTATION_NATURE_DEFAULT; //00 - Budget
    public static String APPLICATION_DECREMENTATION_CAPTURE;//Capture

    public static String APPLICATION_UPDATE_CLOTURE;
    public static String APPLICATION_UPDATE_DATE_CLOTURE;

    public static String APPLICATION_UPDATE_AUTOAFFECTTASK;

    public static String APPLICATION_CLEANCLOSEDTASK;
    public static String APPLICATION_CLEANCLOSEDTASK_CLOTURE_REC;//Clôturée Récursif
    public static String APPLICATION_CLEANCLOSEDTASK_LOT;//Lot de Travaux
    public static String APPLICATION_CLEANCLOSEDTASK_NATURE;//Nature taches
    public static String APPLICATION_CLEANCLOSEDTASK_NATURE_DEFAULT; //00 - Budget

    public static String APPLICATION_MANAGETASKSTEP;
    public static String APPLICATION_MANAGETASKSTEP_TYPE_A; //Jalon PRQ
    public static String APPLICATION_MANAGETASKSTEP_TYPE_B; //Jalon CDRL

    public static String APPLICATION_AUTOADAPTIF;
    public static String APPLICATION_AUTOADAPTIF_SCRIPTMAILCCDC;
    public static String APPLICATION_AUTOADAPTIF_AFFRES_MAX_TAUX;
    public static String APPLICATION_AUTOADAPTIF_AFFRES_ALERTE_TAUX;
    public static String APPLICATION_AUTOADAPTIF_FILTER_DC_FIXE;
    public static String APPLICATION_AUTOADAPTIF_SCRIPTMAILDC;
    public static String APPLICATION_AUTOADAPTIF_AFFRES_ALERTE_EMAIL;
    public static String APPLICATION_AUTOADAPTIF_SCRIPTMAILDEBUTDC;
    public static String APPLICATION_AUTOADAPTIF_FIN_TAA;
    public static String APPLICATION_AUTOADAPTIF_FIN_TAA_GANTT;
    public static String APPLICATION_AUTOADAPTIF_AFFRES_ERROR;
    public static String APPLICATION_AUTOADAPTIF_AFFRES_INACTIF;

    public static void main(String[] args) throws PSException {
        initialisation();
        Logger.info("Demarrage de l'API: " + new Date());
        connection();
        chargementParametreSciforma();
        processProject();
        Logger.info("Fermeture de l'API: " + new Date());
        System.exit(0);
    }

    private static void initialisation() {
        ctx = new FileSystemXmlApplicationContext(System.getProperty("user.dir") + System.getProperty("file.separator") + "config" + System.getProperty("file.separator") + "applicationContext.xml");
    }

    private static void connection() {
        Connector c = (Connector) ctx.getBean("sciforma");

        USER = c.getUSER();
        PWD = c.getPWD();
        IP = c.getIP();
        PORT = c.getPORT();
        CONTEXTE = c.getCONTEXTE();

        try {
            Logger.info("Initialisation de la Session:" + new Date());
            String url = "http://" + IP + ":" + PORT + "/" + CONTEXTE;
            mSession = new Session(url);
            mSession.login(USER, PWD.toCharArray());
            Logger.info("Connecté: " + new Date() + " à l'instance " + CONTEXTE);
        } catch (PSException ex) {
            Logger.error("Erreur dans la connection de ... " + CONTEXTE, ex);
        }
    }

    private static void chargementParametreSciforma() {
        Logger.info("Demarrage du chargement des paramètres de l'application:" + new Date());
        try {
            projectList = (List<Project>) mSession.getProjectList(Project.VERSION_WORKING, Project.READWRITE_ACCESS);
            APPLICATION_HEURE = ((SciformaField) ctx.getBean("application_heure")).getSciformaField();
            APPLICATION_RENUMEROTATION = ((SciformaField) ctx.getBean("application_renumerotation")).getSciformaField();
            APPLICATION_WBS = ((SciformaField) ctx.getBean("application_wbs")).getSciformaField();
            APPLICATION_CAPTURE_BUDGETAIRE = ((SciformaField) ctx.getBean("application_capture")).getSciformaField();
            APPLICATION_DECREMENTATION = ((SciformaField) ctx.getBean("application_decrementation")).getSciformaField();
            APPLICATION_DECREMENTATION_DEFAULT_LOGIN = ((SciformaField) ctx.getBean("application_decrementation_default_login")).getSciformaField();
            APPLICATION_DECREMENTATION_SCRIPTMAILCC = ((SciformaField) ctx.getBean("application_decrementation_scriptmailcc")).getSciformaField();
            APPLICATION_DECREMENTATION_SCRIPTMAIL = ((SciformaField) ctx.getBean("application_decrementation_scriptmail")).getSciformaField();
            APPLICATION_DECREMENTATION_SCRIPTMAILDEBUT = ((SciformaField) ctx.getBean("application_decrementation_scriptmaildebut")).getSciformaField();
            APPLICATION_DECREMENTATION_DELTARW = ((SciformaField) ctx.getBean("application_decrementation_deltarw")).getSciformaField();
            APPLICATION_DECREMENTATION_CAPTURERW = ((SciformaField) ctx.getBean("application_decrementation_capturerw")).getSciformaField();
            APPLICATION_DECREMENTATION_JOB = ((SciformaField) ctx.getBean("application_decrementation_job")).getSciformaField();
            APPLICATION_DECREMENTATION_LOT = ((SciformaField) ctx.getBean("application_decrementation_lot")).getSciformaField();
            APPLICATION_DECREMENTATION_NATURE = ((SciformaField) ctx.getBean("application_decrementation_nature")).getSciformaField();
            APPLICATION_DECREMENTATION_NATURE_DEFAULT = ((SciformaField) ctx.getBean("application_decrementation_nature_default")).getSciformaField();
            APPLICATION_DECREMENTATION_CAPTURE = ((SciformaField) ctx.getBean("application_decrementation_capture")).getSciformaField();
            APPLICATION_UPDATE_CLOTURE = ((SciformaField) ctx.getBean("application_cloture")).getSciformaField();
            APPLICATION_UPDATE_DATE_CLOTURE = ((SciformaField) ctx.getBean("application_cloture_date_cloture")).getSciformaField();
            APPLICATION_UPDATE_AUTOAFFECTTASK = ((SciformaField) ctx.getBean("application_autoaffect")).getSciformaField();
            APPLICATION_CLEANCLOSEDTASK = ((SciformaField) ctx.getBean("application_cleanclosetask")).getSciformaField();
            APPLICATION_CLEANCLOSEDTASK_CLOTURE_REC = ((SciformaField) ctx.getBean("application_cleanclosetask_cloture_rec")).getSciformaField();
            APPLICATION_CLEANCLOSEDTASK_LOT = ((SciformaField) ctx.getBean("application_cleanclosetask_lot")).getSciformaField();
            APPLICATION_CLEANCLOSEDTASK_NATURE = ((SciformaField) ctx.getBean("application_cleanclosetask_nature")).getSciformaField();
            APPLICATION_CLEANCLOSEDTASK_NATURE_DEFAULT = ((SciformaField) ctx.getBean("application_cleanclosetask_nature_default")).getSciformaField();
            APPLICATION_MANAGETASKSTEP = ((SciformaField) ctx.getBean("application_managetaskstep")).getSciformaField();
            APPLICATION_MANAGETASKSTEP_TYPE_A = ((SciformaField) ctx.getBean("application_managetaskstep_type_a")).getSciformaField();
            APPLICATION_MANAGETASKSTEP_TYPE_B = ((SciformaField) ctx.getBean("application_managetaskstep_type_b")).getSciformaField();

            APPLICATION_AUTOADAPTIF = ((SciformaField) ctx.getBean("application_autoadaptif")).getSciformaField();
            APPLICATION_AUTOADAPTIF_SCRIPTMAILCCDC = ((SciformaField) ctx.getBean("application_autoadaptif_scriptmailccdc")).getSciformaField();
            APPLICATION_AUTOADAPTIF_SCRIPTMAILDEBUTDC = ((SciformaField) ctx.getBean("application_autoadaptif_scriptmaildebutdc")).getSciformaField();
            APPLICATION_AUTOADAPTIF_AFFRES_MAX_TAUX = ((SciformaField) ctx.getBean("APPLICATION_AUTOADAPTIF_AFFRES_MAX_TAUX")).getSciformaField();
            APPLICATION_AUTOADAPTIF_FILTER_DC_FIXE = ((SciformaField) ctx.getBean("application_autoadaptif_filter_dc_fixe")).getSciformaField();
            APPLICATION_AUTOADAPTIF_SCRIPTMAILDC = ((SciformaField) ctx.getBean("application_autoadaptif_scriptmaildc")).getSciformaField();
            APPLICATION_AUTOADAPTIF_AFFRES_ALERTE_TAUX = ((SciformaField) ctx.getBean("APPLICATION_AUTOADAPTIF_AFFRES_ALERTE_TAUX")).getSciformaField();
            APPLICATION_AUTOADAPTIF_AFFRES_ALERTE_EMAIL = ((SciformaField) ctx.getBean("APPLICATION_AUTOADAPTIF_AFFRES_ALERTE_EMAIL")).getSciformaField();
            APPLICATION_AUTOADAPTIF_FIN_TAA = ((SciformaField) ctx.getBean("APPLICATION_AUTOADAPTIF_FIN_TAA")).getSciformaField();
            APPLICATION_AUTOADAPTIF_FIN_TAA_GANTT = ((SciformaField) ctx.getBean("APPLICATION_AUTOADAPTIF_FIN_TAA_GANTT")).getSciformaField();
            APPLICATION_AUTOADAPTIF_AFFRES_ERROR = ((SciformaField) ctx.getBean("APPLICATION_AUTOADAPTIF_AFFRES_ERROR")).getSciformaField();
            APPLICATION_AUTOADAPTIF_AFFRES_INACTIF = ((SciformaField) ctx.getBean("APPLICATION_AUTOADAPTIF_AFFRES_INACTIF")).getSciformaField();

            Logger.info("Chargement des projets [VERSION_WORKING][READWRITE_ACCESS]");
            sdf = new SimpleDateFormat("dd/MM/yyyy");
            Calendar cEnd = Calendar.getInstance();
            today = cEnd.getTime();
        } catch (InvalidDataException ex) {
            Logger.error("Erreur dans la méthode: chargementParametreSciforma", ex);
        } catch (PSException ex) {
            Logger.error("Erreur dans la méthode: chargementParametreSciforma", ex);
        }
    }

    private static void processProject() throws PSException {
        int nbProjet = projectList.size();
        for (Project project : projectList) {
            try {
                project.open(false);
                //if(project.getStringField("Name").contains("***")){
                Logger.info("=======================================================================================");
                Logger.info("Traitement du projet [" + (projectList.indexOf(project) + 1) + "/" + nbProjet + "] " + project.getStringField("Name"));
                Logger.info("=======================================================================================");
                if (project.getBooleanField(APPLICATION_HEURE)) {
                    applicationHeures(project);
                }
                if (project.getBooleanField(APPLICATION_WBS)) {
                    applicationWBS(project);
                }
                if (project.getBooleanField(APPLICATION_RENUMEROTATION)) {
                    applicationRenumerotation(project);
                }
                if (project.getBooleanField(APPLICATION_DECREMENTATION)) {
                    applicationDecrementationBudgetaire(project);
                }
                if (project.getBooleanField(APPLICATION_UPDATE_CLOTURE)) {
                    applicationUpdateCloture(project);
                }
                if (project.getBooleanField(APPLICATION_UPDATE_AUTOAFFECTTASK)) {
                    applicationUpdateAutoaffectTask(project);
                }
                if (project.getBooleanField(APPLICATION_CLEANCLOSEDTASK)) {
                    applicationCleanClosedTaks(project);
                }
                if (project.getBooleanField(APPLICATION_MANAGETASKSTEP)) {
                    applicationManageTaskStep(project);
                }
                if (project.getBooleanField(APPLICATION_AUTOADAPTIF)) {
                    applicationAutoAdaptif(project);
                }

                Logger.info("Sauvegarde du projet " + project.getStringField("Name"));
                project.save();
                Logger.info("Publication du projet " + project.getStringField("Name"));
                project.publish();
                //}
            } catch (PSException ex) {
                Logger.error(ex);
            } finally {
                project.close();
                updateGlobal();
            }
        }
    }

    private static void applicationHeures(Project project) {
        Logger.info("Start applicationHeures");
        try {
            if (today.compareTo(project.getDateField("Start")) > 0) {
                //System.out.println("today = " + today);
                project.applyTimesheets(project.getDateField("Start"), today, true, true, false);
            } else {
                Logger.error("ERROR: " + project.getStringField("Name") + " Begin <" + sdf.format(project.getDateField("Start")) + "> and today <" + sdf.format(today) + ">");
            }
        } catch (PSException ex) {
            Logger.error(ex);
        }
        Logger.info("End applicationHeures");
    }

    private static void applicationWBS(Project project) {
        Logger.info("Start applicationWBS");
        try {
            List<Task> taskList = project.getTaskOutlineList();
            TaskOutlineList children;
            children = (TaskOutlineList) taskList;
            int prefixe = 1;
            for (Task iTask : taskList) {
                if (iTask.getIntField("Outline Level") == 1) {
                    iTask.setStringField("WBS", String.valueOf(prefixe));
                    prefixe++;
                }
                if (children.getChildCount(iTask) > 0) {
                    childrenEncoding(taskList, children, iTask);
                }
            }
        } catch (PSException e) {
            Logger.error("Problème rencontré à la création du code WBS parent dû à :", e);
        }
        Logger.info("End applicationWBS");
    }

    /*
     * Traitement des Activités enfants
     */
    private static void childrenEncoding(List<Task> taskList,
            TaskOutlineList children, Task iTask) {
        try {
            for (int k = 1; k <= children.getChildCount(iTask); k++) {
                taskList.get(taskList.indexOf(iTask) + k).setStringField("WBS", iTask.getStringField("WBS").concat(String.valueOf("." + k)));
            }
        } catch (PSException e) {
            Logger.error("Problème rencontré à la création du code WBS enfant dû à:", e);
        }

    }

    private static void applicationRenumerotation(Project project) {
        Logger.info("Start applicationRenumerotation");
        int numero = 1;
        int sup = 100000;
        try {
            List<Task> taskList = project.getTaskOutlineList();
            for (Task task : taskList) {
                task.setIntField("#", sup);
                sup++;
            }
            for (Task task : taskList) {
                task.setIntField("#", numero);
                numero++;
            }

        } catch (PSException e) {
            Logger.error("Problème rencontrer pendant la renumérotation du à :", e);
        }
        Logger.info("End applicationRenumerotation");
    }

    private static void applicationCapture(Project project) {
        Logger.info("Start applicationCapture");
        try {
            TaskOutlineList tasks = project.getTaskOutlineList();
            Iterator taskIt = tasks.iterator();
            while (taskIt.hasNext()) {
                Task t = (Task) taskIt.next(); //Capture Rolling Wave
                //Logger.info( "applicationCapture - " + t.getStringField("Name"));
                List<ResAssignment> resAssignList = t.getResAssignmentList();
                Iterator resIt = resAssignList.iterator();
                while (resIt.hasNext()) {
                    ResAssignment res = (ResAssignment) resIt.next();
                    //Logger.info( "applicationCapture - " + res.getDoubleField("Total Effort"));
                    res.setDoubleField(APPLICATION_DECREMENTATION_CAPTURERW, res.getDoubleField("Total Effort"));
                }
            }
            project.setBooleanField(APPLICATION_DECREMENTATION_CAPTURE, true);
        } catch (PSException e) {
            Logger.error("captureBudgetaire :", e);
        }
        Logger.info("End applicationCapture");
    }

    private static void applicationDecrementation(Project project) {
        try {
            Logger.info("Start applicationDecrementation");
            Boolean reserveOk = false;
            Boolean lotOk = false;
            Task taskReserve = null;
            Task taskLot = null;
            Global g = new Global();
            g.lock();
            List l = new ArrayList();
            l.add(APPLICATION_DECREMENTATION_DEFAULT_LOGIN);
            try {
                g.setListField(APPLICATION_DECREMENTATION_SCRIPTMAILCC, l);
            } catch (DataFormatException ex) {
                java.util.logging.Logger.getLogger(Run.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (PSException ex) {
                java.util.logging.Logger.getLogger(Run.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
            try {
                TaskOutlineList tasks = project.getTaskOutlineList();
                Iterator taskIt = tasks.iterator();
                while (taskIt.hasNext()) {
                    Task t = (Task) taskIt.next();
                    if (t.getBooleanField(APPLICATION_DECREMENTATION_LOT)) {
                        taskLot = t;
                        lotOk = true;
                        Logger.info("Traitement du lot: " + t.getStringField("Name"));
                    }
                    if (lotOk) {
                        if (t.getStringField(APPLICATION_DECREMENTATION_NATURE).equals(APPLICATION_DECREMENTATION_NATURE_DEFAULT)) {
                            if (t.getResAssignmentList().size() > 0) {
                                taskReserve = t;
                                reserveOk = true;
                                Logger.info("Réserve trouvé : " + t.getStringField("Name"));
                            }
                        } else {
                            if (taskLot.getStringField("Work Package ID").equals(t.getStringField("Work Package ID"))) {
                                if (reserveOk) {
                                    List<ResAssignment> resAssignList = t.getResAssignmentList();
                                    Iterator resIt = resAssignList.iterator();
                                    while (resIt.hasNext()) {
                                        ResAssignment res = (ResAssignment) resIt.next();
                                        res.setDoubleField(APPLICATION_DECREMENTATION_DELTARW, res.getDoubleField("Total Effort") - res.getDoubleField(APPLICATION_DECREMENTATION_CAPTURERW));
                                        //On retire de la reserve de notre activité budget
                                        List<ResAssignment> resReserveAssignList = taskReserve.getResAssignmentList();
                                        Iterator resReserveIT = resReserveAssignList.iterator();
                                        while (resReserveIT.hasNext()) {
                                            ResAssignment resReserve = (ResAssignment) resReserveIT.next();
                                            if (resReserve.getStringField(APPLICATION_DECREMENTATION_JOB).equals(res.getStringField(APPLICATION_DECREMENTATION_JOB))) {
                                                if (resReserve.getDoubleField("Total Effort") > res.getDoubleField(APPLICATION_DECREMENTATION_DELTARW)) {
                                                    //On retire le Delta de la reserve
                                                    resReserve.setDoubleField("Total Effort", resReserve.getDoubleField("Total Effort") - res.getDoubleField(APPLICATION_DECREMENTATION_DELTARW));
                                                    if (res.getDoubleField(APPLICATION_DECREMENTATION_DELTARW) != 0) {
                                                        Logger.info("Décrémentation de la réserve " + t.getStringField("Name") + " =>" + res.getDoubleField(APPLICATION_DECREMENTATION_DELTARW));
                                                    }
                                                    if (res.getDoubleField(APPLICATION_DECREMENTATION_DELTARW) > 0) {
                                                        Logger.info("On retire le Delta de la reserve : " + String.valueOf(resReserve.getDoubleField("Total Effort") - res.getDoubleField(APPLICATION_DECREMENTATION_DELTARW)));
                                                    }
                                                } else {
                                                    //Si le Delta est trop grand, on met la reserve à 0 et on envoie un mail
                                                    if (resReserve.getDoubleField("Total Effort") > 0) {
                                                        String message = g.getStringField(APPLICATION_DECREMENTATION_SCRIPTMAIL);
                                                        message += "La réservation budgétaire de  ";
                                                        message += taskReserve.getStringField("Name");
                                                        message += " pour le métier ";
                                                        message += resReserve.getStringField(APPLICATION_DECREMENTATION_JOB);
                                                        message += " est épuisée. <br/>";
                                                        Logger.info(message);
                                                        String messageDeb = "Concerne le projet: " + project.getStringField("Name");
                                                        if (!taskLot.getStringField("Manager 1").equals("")) {
                                                            l.add(taskLot.getStringField("Manager 1"));
                                                        }
                                                        g.setStringField(APPLICATION_DECREMENTATION_SCRIPTMAIL, message);
                                                        g.setStringField(APPLICATION_DECREMENTATION_SCRIPTMAILDEBUT, messageDeb);
                                                        Logger.info("La réserve est repassé à 0 envoie d'un email:" + message);
                                                    }
                                                    resReserve.setDoubleField("Total Effort", 0);
                                                }
                                                res.setDoubleField(APPLICATION_DECREMENTATION_DELTARW, 0);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                g.setListField(APPLICATION_DECREMENTATION_SCRIPTMAILCC, l);
                g.save(true);
            } catch (PSException e) {
                Logger.error("decrementationBudgetaire :", e);
            }
            Logger.info("End applicationDecrementation");
        } catch (PSException ex) {
            Logger.error(ex);
        }
    }

    private static void applicationUpdateCloture(Project project) {
        Logger.info("Start applicationUpdateCloture");
        try {
            TaskOutlineList tasks = project.getTaskOutlineList();
            Iterator taskIt = tasks.iterator();
            while (taskIt.hasNext()) {
                Task t = (Task) taskIt.next();
                if (!(t.getDateField(APPLICATION_UPDATE_DATE_CLOTURE).getTime() > 0) && t.getBooleanField("Closed")) {
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DATE, -1);
                    t.setDateField(APPLICATION_UPDATE_DATE_CLOTURE, cal.getTime());
                }
                if (t.getDateField(APPLICATION_UPDATE_DATE_CLOTURE).getTime() > 0 && !t.getBooleanField("Closed")) {
                    t.setDateField(APPLICATION_UPDATE_DATE_CLOTURE, null);
                }
            }
        } catch (PSException ex) {
            Logger.error(ex);
        }
        Logger.info("End applicationUpdateCloture");
    }

    private static void applicationUpdateAutoaffectTask(Project project) {
        Logger.info("Start applicationUpdateAutoaffectTask");
        try {
            TaskOutlineList tasks = project.getTaskOutlineList();
            Iterator taskIt = tasks.iterator();
            while (taskIt.hasNext()) {
                Task t = (Task) taskIt.next();
                if (t.getBooleanField("Closed") || t.getBooleanField("Is Parent")) {
                    t.setBooleanField("Allow My Work Add", false);
                }
            }
        } catch (PSException ex) {
            Logger.error(ex);
        }
        Logger.info("End applicationUpdateAutoaffectTask");
    }

    private static void applicationCleanClosedTaks(Project project) {
        Logger.info("Start applicationCleanClosedTaks");
        try {
            project.setBooleanField("Actuals from Tracking Source Only", false);
            TaskOutlineList tasks = project.getTaskOutlineList();
            Iterator taskIt = tasks.iterator();
            while (taskIt.hasNext()) {
                Task t = (Task) taskIt.next();
                if (t.getBooleanField(APPLICATION_CLEANCLOSEDTASK_CLOTURE_REC)) {
                    Logger.info(t.getStringField("Name") + " Clôturée Récursif");
                    if (t.getStringField("Schedule Type").equals("Hammock")) {
                        t.setStringField("Schedule Type", "ASAP");
                        t.setDateField("Finish", t.getDateField("Completed Date"));
                    }
                    if (t.getBooleanField("Is Parent") && t.getResAssignmentList().size() > 0) {
                        Task parent = new Task("AUTO_" + t.getStringField("Name"), "AUTO" + t.getStringField("ID"), project);
                        parent.setBooleanField("Closed", true);
                        tasks.add(tasks.indexOf(t), parent);
                        tasks.setOutlineLevel(parent, t.getIntField("Outline Level"));
                        tasks.setOutlineLevel(t, t.getIntField("Outline Level") + 1);
                        taskIt = tasks.iterator();
                    }
                    if (!t.getBooleanField("Is Parent")) {
                        if (t.getDateField("Finish").after(today) && t.getDateField("Start").before(today)) {
                            t.setDateField("Finish", today);
                        } else {
                            if (t.getDateField("Start").after(today)) {
                                t.setDateField("Must Start On", today);
                                t.setDateField("Finish", today);
                            }
                        }
                    }
                    List<ResAssignment> resAssignList = t.getResAssignmentList();
                    Iterator resIt = resAssignList.iterator();
                    while (resIt.hasNext()) {
                        ResAssignment res = (ResAssignment) resIt.next();
                        res.setDoubleField("Remaining Effort", 0.0);
                    }
                    List<CostAssignment> costAssignList = t.getCostAssignmentList();
                    Iterator costIt = costAssignList.iterator();
                    while (costIt.hasNext()) {
                        CostAssignment cost = (CostAssignment) costIt.next();
                        cost.setDoubleField("Remaining Units", 0.0);
                    }
                    t.setBooleanField("Allow Splitting", false);
                    if (!t.getBooleanField("Is Parent")) {
                        t.setDoubleField("% Completed", 1.0);
                    }
                    t.setBooleanField("Closed", true);
                    if (t.getBooleanField(APPLICATION_CLEANCLOSEDTASK_LOT)) {
                        t.setStringField("Manager 1", "");
                        t.setStringField("Manager 2", "");
                        t.setStringField("Manager 3", "");
                    }
                    if (t.getStringField(APPLICATION_CLEANCLOSEDTASK_NATURE).equals(APPLICATION_CLEANCLOSEDTASK_NATURE_DEFAULT)) {
                        t.getPredecessorLinksList().clear();
                        t.getSuccessorLinksList().clear();
                    }
                }
            }
            project.setBooleanField("Actuals from Tracking Source Only", true);
        } catch (PSException ex) {
            Logger.error(ex);
        }
        Logger.info("End applicationCleanClosedTaks");
    }

    private static void applicationManageTaskStep(Project project) {
        Logger.info("Start applicationManageTaskStep");
        try {
            TaskOutlineList tasks = project.getTaskOutlineList();
            Iterator taskIt = tasks.iterator();
            while (taskIt.hasNext()) {
                Task t = (Task) taskIt.next();
                if (t.getBooleanField(APPLICATION_MANAGETASKSTEP_TYPE_A) || t.getBooleanField(APPLICATION_MANAGETASKSTEP_TYPE_B)) {
                    if (t.getTaskStepList().isEmpty()) {
                        TaskStep etape = new TaskStep(t);
                        etape.setStringField("Name", t.getStringField("Name"));
                        etape.setDateField("Objectif", t.getDateField("Required Date"));
                        t.getTaskStepList().add(etape);
                    } else {
                        Iterator etapeIt = t.getTaskStepList().iterator();
                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.YEAR, 2099);
                        Date d = cal.getTime();
                        while (etapeIt.hasNext()) {
                            TaskStep e = (TaskStep) etapeIt.next();
                            if (d.after(e.getDateField("Objectif"))) {
                                d = e.getDateField("Objectif");
                            }
                        }
                        t.setDateField("Required Date", d);
                    }

                }
            }
        } catch (PSException ex) {
            Logger.error(ex);
        }
        Logger.info("End applicationManageTaskStep");
    }

    private static void applicationDecrementationBudgetaire(Project project) {
        Logger.info("Start applicationDecrementationBudgetaire");
        try {
            if (!project.getBooleanField(APPLICATION_DECREMENTATION_CAPTURE)) {
                Logger.info("Projet pas encore capturé");
                applicationCapture(project);
                project.setBooleanField(APPLICATION_DECREMENTATION_CAPTURE, true);
            }
            if (project.getBooleanField(APPLICATION_DECREMENTATION_CAPTURE)) {
                applicationDecrementation(project);
                applicationCapture(project);
            }
        } catch (PSException ex) {
            Logger.error(ex);
        }
        Logger.info("End applicationDecrementationBudgetaire");
    }

    private static void updateGlobal() {
        Global g = new Global();
        try {
            g.lock();
            g.setStringField(APPLICATION_DECREMENTATION_SCRIPTMAIL, "");
            g.setStringField(APPLICATION_DECREMENTATION_SCRIPTMAILDEBUT, "");
            g.save(true);
        } catch (PSException ex) {
            Logger.error(ex);
        }

    }

    private static void applicationAutoAdaptif(Project project) {
        Logger.info("Start applicationAutoAdaptif");
        //updateTypeAffectation(project);
        processUpdateDureeChargeFixee(project);
        Logger.info("End applicationAutoAdaptif");
    }

    private static void updateTypeAffectation(Project project) {
        try {
            TaskOutlineList tasks = project.getTaskOutlineList();
            Iterator taskIt = tasks.iterator();
            while (taskIt.hasNext()) {
                Task t = (Task) taskIt.next();
                if (!t.getStringField("Schedule Type").equals("Hammock") && !t.getBooleanField("Is Parent")) {
                    List<ResAssignment> resAssignList = t.getResAssignmentList();
                    Iterator resIt = resAssignList.iterator();
                    while (resIt.hasNext()) {
                        ResAssignment res = (ResAssignment) resIt.next();
                        res.setStringField("Distribution Type", "Fixed Duration-Effort");
                    }
                }
            }
        } catch (PSException ex) {
            Logger.error(ex);
        }
    }

    private static void processUpdateDureeChargeFixee(Project project) {
        try {
            Boolean updateFinishDate = true;
            Boolean updateTauxMax = false;
            Date taskFinishDate = null;
            String distribution = "";
            Global g = new Global();
            g.setStringField(APPLICATION_AUTOADAPTIF_SCRIPTMAILDC, "");
            //project.updateProgress(today, false, true);
            TaskOutlineList tasks = project.getTaskOutlineList();
            Iterator taskIt = tasks.iterator();
            while (taskIt.hasNext()) {
                updateFinishDate = true;
                updateTauxMax = false;
                distribution = "";
                Task t = (Task) taskIt.next();
                Logger.info("Lecture de la Task " + t.getStringField("Name"));
                taskFinishDate = t.getDateField(APPLICATION_AUTOADAPTIF_FIN_TAA_GANTT);
                //Fixed Effort
                //Fixed Duration-Effort
                //Fixed Effort-Rate         
                //double taux = t.getDoubleField(APPLICATION_AUTOADAPTIF_ACT_MAX_TAUX);                
                if (t.getBooleanField(APPLICATION_AUTOADAPTIF_FILTER_DC_FIXE)) {
                    Logger.info("La task répond au filtre TAA Filtre Report");
                    updateFinishDate = true;

                    List<ResAssignment> resAssignList = t.getResAssignmentList();
                    Iterator resIt = resAssignList.iterator();
                    while (resIt.hasNext()) {
                        ResAssignment res = (ResAssignment) resIt.next();
                        if (t.getBooleanField("TAA vide")) {
                            updateFinishDate = false;
                            updateTauxMax = true;
                            Logger.info("#01: APPLICATION_AUTOADAPTIF_FIN_TAA_GANTT vide " + t.getStringField("Name"));
                        } else {
                            if (res.getDoubleField("Rate") > res.getDoubleField(APPLICATION_AUTOADAPTIF_AFFRES_MAX_TAUX)) {
                                updateFinishDate = false;
                                Logger.info("#02: Rate(" + res.getDoubleField("Rate") + ") > APPLICATION_AUTOADAPTIF_AFFRES_MAX_TAUX (" + res.getDoubleField(APPLICATION_AUTOADAPTIF_AFFRES_MAX_TAUX) + ") pour la task " + t.getStringField("Name"));
                            }
                            if (taskFinishDate.before(project.getDateField("As Of Date"))) {
                                updateFinishDate = false;
                                updateTauxMax = true;
                                Logger.info("#03: APPLICATION_AUTOADAPTIF_FIN_TAA_GANTT before As Of Date pour la task " + t.getStringField("Name"));
                            }
                            if (t.getDateField("Finish").before(taskFinishDate)) {
                                updateFinishDate = false;
                                Logger.info("#04: Finish before APPLICATION_AUTOADAPTIF_FIN_TAA_GANTT pour la task " + t.getStringField("Name"));
                            }
                            if (taskFinishDate.before(t.getDateField("Start"))) {
                                updateFinishDate = false;
                                updateTauxMax = true;
                                Logger.info("#05: APPLICATION_AUTOADAPTIF_FIN_TAA_GANTT before Start pour la task " + t.getStringField("Name"));
                                ///si le As Of Date du projet > fin taa alors date de fin activité = as of date du projet                          
                            }
                            if (taskFinishDate.before(res.getDateField("Remaining Start"))) {
                                Logger.info("#06: APPLICATION_AUTOADAPTIF_FIN_TAA_GANTT before Remaining Start pour la task " + t.getStringField("Name"));
                                /**
                                 * Modifcation suite au mail du 6 décembre 2018 à 14:22
                                 * updateFinishDate = false;
                                 * updateTauxMax = true; 
                                 */
                                taskFinishDate = res.getDateField("Remaining Start");
                                /**
                                 * Modifcation suite au mail du 11 décembre 2018 à 23:39                 
                                 */
                                Logger.info("La date fin du Remaining Start:" + taskFinishDate.toString());
                                Logger.info("On ajoute 1 jour");
                                Calendar c = Calendar.getInstance();
                                c.setTime(taskFinishDate);
                                c.add(Calendar.DATE, 1);
                                taskFinishDate = c.getTime();
                                Logger.info("On appliquera la date fin du " + taskFinishDate.toString());
                            }
                        }
                    }
                    if (updateFinishDate) {
                        Logger.info("Traitement de: " + t.getStringField("Name") + " | Ancienne date de fin: " + t.getDateField("Finish").toString() + " | Nouvelle date de fin: " + taskFinishDate.toString());
                        t.setDateField("Finish", taskFinishDate);
                    }
                    String message = g.getStringField(APPLICATION_AUTOADAPTIF_SCRIPTMAILDC);
                    String messageDeb = "Concerne le projet: " + project.getStringField("Name");
                    resIt = resAssignList.iterator();
                    while (resIt.hasNext()) {
                        ResAssignment res = (ResAssignment) resIt.next();
                        distribution = res.getStringField("Distribution Type");
                        if (!res.getBooleanField(APPLICATION_AUTOADAPTIF_AFFRES_INACTIF) && !res.getBooleanField(APPLICATION_AUTOADAPTIF_AFFRES_ERROR)) {
                            if (res.getDoubleField("Rate") >= res.getDoubleField(APPLICATION_AUTOADAPTIF_AFFRES_MAX_TAUX)) {
                                message += "Dépassement du Taux Max pour le metier: " + res.getStringField("Métier direct") + " pour l'activité " + t.getStringField("Name");
                                message += ". <br/>";
                                Logger.info("#07 - Dépassement du Taux Max (Taux actuel:" + res.getDoubleField("Rate") + ") pour le metier: " + res.getStringField("Métier direct") + " pour l'activité " + t.getStringField("Name"));
                                res.setStringField("Distribution Type", "Fixed Rate-Effort");
                                res.setDoubleField("Rate", res.getDoubleField(APPLICATION_AUTOADAPTIF_AFFRES_MAX_TAUX));
                                res.setStringField("Distribution Type", distribution);
                                Logger.info("Nouveau taux: " + res.getDoubleField("Rate"));
                            } else if (res.getDoubleField("Rate") > res.getDoubleField(APPLICATION_AUTOADAPTIF_AFFRES_ALERTE_TAUX) && !res.getBooleanField(APPLICATION_AUTOADAPTIF_AFFRES_ALERTE_EMAIL)) {
                                message += "Dépassement du Taux Alerte pour le metier :" + res.getStringField("Métier direct") + " pour l'activité " + t.getStringField("Name");
                                message += ". <br/>";
                                res.setBooleanField(APPLICATION_AUTOADAPTIF_AFFRES_ALERTE_EMAIL, true);
                            }
                            if (res.getDoubleField("Rate") < res.getDoubleField(APPLICATION_AUTOADAPTIF_AFFRES_ALERTE_TAUX)) {
                                res.setBooleanField(APPLICATION_AUTOADAPTIF_AFFRES_ALERTE_EMAIL, true);
                            }
                            if (updateTauxMax) {
                                Logger.info("#08 - On force l'activité au taux max");
                                res.setStringField("Distribution Type", "Fixed Rate-Effort");
                                res.setDoubleField("Rate", res.getDoubleField(APPLICATION_AUTOADAPTIF_AFFRES_MAX_TAUX));
                                res.setStringField("Distribution Type", distribution);
                            }
                            /**
                             Mail du 20-12-18 17:08 
                             Ça marche effectivement mieux comme ça! 
                            Par contre, la charge n’étant plus mise à 0, la ligne 24 du test dynamique fait apparaître un autre bug (doh !) qui n’a rien à voir avec le report de charge.

                            Explication :
                            Soit une activité comme ci-dessous, sa fin TAA est au 1/11/18

                            Une affectation est en avance mais avec un taux > max
                            Une affectation est en retard, mais avec un taux < max
                            La date de fin de l’activité étant > fin TAA, la routine devrait forcer la date de fin à fin TAA
                            Mais, comme une affectation est en taux > taux max :
                                          if (res.getDoubleField("Rate") > res.getDoubleField(APPLICATION_AUTOADAPTIF_AFFRES_MAX_TAUX))
                                          {
                                            updateFinishDate = Boolean.valueOf(false);
                            cette condition demande de ne pas forcer la date de fin de l’activité

                            L’affectation restera en retard, alors que revenir à fin TAA est possible

                            (j’ai créé une ligne spécifique pour mettre ce problème en exergue, mais je n’arrive pas à l’exécuter car l’IT n’a pas encore installé les nlles licences sur DEV. Je te confirme ça dès que je peux)

                             */
                            if(res.getDateField("Scheduled Finish").after(taskFinishDate) && res.getDoubleField("Rate") < res.getDoubleField(APPLICATION_AUTOADAPTIF_AFFRES_MAX_TAUX)){
                                Logger.info("#09 - On force l'affectation au taux max");
                                res.setStringField("Distribution Type", "Fixed Rate-Effort");
                                res.setDoubleField("Rate", res.getDoubleField(APPLICATION_AUTOADAPTIF_AFFRES_MAX_TAUX));
                                res.setStringField("Distribution Type", distribution);
                            }
                        }

                    }
                    g.lock();
                    g.setStringField(APPLICATION_AUTOADAPTIF_SCRIPTMAILDC, message);
                    g.setStringField(APPLICATION_AUTOADAPTIF_SCRIPTMAILDEBUTDC, messageDeb);
                    g.save(true);
                }else
                {
                    Logger.info("La task ne répond pas au filtre TAA Filtre Report");
                }
            }
        } catch (PSException ex) {
            Logger.error(ex);
        }
    }

}
