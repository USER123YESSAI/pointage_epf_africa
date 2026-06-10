package sn.epf.pointage.ui;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sn.epf.pointage.config.SessionContext;
import sn.epf.pointage.dao.CoursDAO;
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.dao.SalleDAO;
import sn.epf.pointage.model.Assignation;
import sn.epf.pointage.model.Cours;
import sn.epf.pointage.model.PeriodiciteCours;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.Salle;
import sn.epf.pointage.model.enums.FrequenceCours;

public class AssignationFormController {

    @FXML private ComboBox<Professeur> comboProfesseur;
    @FXML private ComboBox<Cours> comboCours;
    @FXML private ComboBox<Salle> comboSalle;

    @FXML private TextField fieldAnneeAcademique;
    @FXML private TextField fieldHeuresPrevues;

    @FXML private ComboBox<DayOfWeek> comboJour;
    @FXML private TextField fieldHeureDebut;
    @FXML private TextField fieldHeureFin;
    @FXML private ComboBox<FrequenceCours> comboFrequence;

    @FXML private DatePicker dateDebutSemestre;
    @FXML private DatePicker dateFinSemestre;

    @FXML private Label lblErreur;

    private final ProfesseurDAO professeurDAO = new ProfesseurDAO();
    private final CoursDAO coursDAO = new CoursDAO();
    private final SalleDAO salleDAO = new SalleDAO();

    private final EnrolementServiceWrapper enrolementService = new EnrolementServiceWrapper();
    private boolean saved = false;

    @FXML
    public void initialize() {
        try {
            chargerProfesseurs();
            chargerCours();
            chargerSalles();

            comboJour.setItems(FXCollections.observableArrayList(Arrays.asList(DayOfWeek.values())));
            comboJour.setValue(DayOfWeek.MONDAY);

            comboFrequence.setItems(FXCollections.observableArrayList(FrequenceCours.values()));
            comboFrequence.setValue(FrequenceCours.HEBDO);

            dateDebutSemestre.setValue(LocalDate.now());
            dateFinSemestre.setValue(LocalDate.now().plusMonths(4));
        } catch (Exception e) {
            showErreur("Erreur init formulaire : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void chargerProfesseurs() {
        // Si PROFESSEUR connecté : limiter à son profil
        SessionContext ctx = SessionContext.getInstance();
        if (ctx.isProfesseur() && ctx.getUtilisateurConnecte() != null && ctx.getUtilisateurConnecte().getProfesseurLie() != null) {
            comboProfesseur.setItems(FXCollections.observableArrayList(ctx.getUtilisateurConnecte().getProfesseurLie()));
            comboProfesseur.setValue(ctx.getUtilisateurConnecte().getProfesseurLie());
            comboProfesseur.setDisable(true);
            comboProfesseur.setManaged(false);
        } else {
            List<Professeur> profs = professeurDAO.findAllActifs();
            comboProfesseur.setItems(FXCollections.observableArrayList(profs));
        }
    }

    private void chargerCours() {
        comboCours.setItems(FXCollections.observableArrayList(coursDAO.findAll()));
    }

    private void chargerSalles() {
        comboSalle.setItems(FXCollections.observableArrayList(salleDAO.findAll()));
    }

    @FXML
    public void handleSauvegarder() {
        try {
            clearErreur();

            Professeur professeur = comboProfesseur.getValue();
            Cours cours = comboCours.getValue();
            Salle salle = comboSalle.getValue();

            if (professeur == null) throw new IllegalArgumentException("Choisissez un professeur.");
            if (cours == null) throw new IllegalArgumentException("Choisissez un cours.");
            if (salle == null) throw new IllegalArgumentException("Choisissez une salle.");

            String anneeAcademique = fieldAnneeAcademique.getText() == null ? "" : fieldAnneeAcademique.getText().trim();
            if (anneeAcademique.isBlank()) throw new IllegalArgumentException("Année académique obligatoire.");

            int heuresPrevues;
            try {
                heuresPrevues = Integer.parseInt(fieldHeuresPrevues.getText().trim());
            } catch (Exception e) {
                throw new IllegalArgumentException("Heures prévues invalide.");
            }

            DayOfWeek jour = comboJour.getValue();
            FrequenceCours frequence = comboFrequence.getValue();

            LocalTime heureDebut = parseHeure(fieldHeureDebut.getText(), "Heure début");
            LocalTime heureFin = parseHeure(fieldHeureFin.getText(), "Heure fin");
            int dureeMinutes = (int) Duration.between(heureDebut, heureFin).toMinutes();
            if (dureeMinutes <= 0) throw new IllegalArgumentException("La durée doit être positive (heure fin > heure début)." );

            LocalDate dateDebutSemestreV = dateDebutSemestre.getValue();
            LocalDate dateFinSemestreV = dateFinSemestre.getValue();
            if (dateDebutSemestreV == null || dateFinSemestreV == null) {
                throw new IllegalArgumentException("Dates de semestre obligatoires.");
            }
            if (dateFinSemestreV.isBefore(dateDebutSemestreV)) {
                throw new IllegalArgumentException("Date fin semestre doit être >= date début semestre.");
            }

            PeriodiciteCours periodicite = new PeriodiciteCours();
            periodicite.setJourSemaine(jour);
            periodicite.setHeureDebut(heureDebut);
            periodicite.setHeureFin(heureFin);
            periodicite.setFrequence(frequence);
            periodicite.setDureeMinutes(dureeMinutes);

            enrolementService.assignerCours(professeur, cours, salle,
                    anneeAcademique, heuresPrevues,
                    periodicite, dateDebutSemestreV, dateFinSemestreV);

                // Indiquer que l'objet a été sauvegardé puis fermer
                this.saved = true;
                closeWindow();
        } catch (Exception e) {
            showErreur(e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAnnuler() {
        closeWindow();
    }

    private LocalTime parseHeure(String txt, String label) {
        if (txt == null || txt.isBlank()) throw new IllegalArgumentException(label + " obligatoire.");
        try {
            return LocalTime.parse(txt.trim(), DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            throw new IllegalArgumentException(label + " invalide (format attendu HH:mm)." );
        }
    }

    private void showErreur(String msg) {
        lblErreur.setText(msg);
        lblErreur.setVisible(true);
        lblErreur.setManaged(true);
    }

    private void clearErreur() {
        lblErreur.setText("");
        lblErreur.setVisible(false);
        lblErreur.setManaged(false);
    }

    private void closeWindow() {
        Stage stage = (Stage) comboProfesseur.getScene().getWindow();
        stage.close();
    }

    /** wrapper pour éviter import service partout */
    private static class EnrolementServiceWrapper {
        private final sn.epf.pointage.service.EnrolementService service = new sn.epf.pointage.service.EnrolementService();
        public Assignation assignerCours(Professeur professeur, Cours cours, Salle salle,
                                          String anneeAcademique, int heuresPrevues,
                                          PeriodiciteCours periodicite,
                                          LocalDate dateDebutSemestre, LocalDate dateFinSemestre) {
            return service.assignerCours(professeur, cours, salle,
                    anneeAcademique, heuresPrevues,
                    periodicite,
                    dateDebutSemestre, dateFinSemestre);
        }
    }

    public boolean isSaved() { return saved; }
}

