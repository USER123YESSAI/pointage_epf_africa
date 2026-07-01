package sn.epf.pointage.ui;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
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
import sn.epf.pointage.service.EnrolementService;

public class AssignationFormController {

    @FXML private ComboBox<Professeur>    comboProfesseur;
    @FXML private ComboBox<Cours>         comboCours;
    @FXML private ComboBox<Salle>         comboSalle;
    @FXML private TextField               fieldAnneeAcademique;
    @FXML private TextField               fieldHeuresPrevues;
    @FXML private ComboBox<DayOfWeek>     comboJour;
    @FXML private TextField               fieldHeureDebut;
    @FXML private TextField               fieldHeureFin;
    @FXML private ComboBox<FrequenceCours> comboFrequence;
    @FXML private DatePicker              dateDebutSemestre;
    @FXML private DatePicker              dateFinSemestre;
    @FXML private Label                   lblErreur;

    private final ProfesseurDAO    professeurDAO    = new ProfesseurDAO();
    private final CoursDAO         coursDAO         = new CoursDAO();
    private final SalleDAO         salleDAO         = new SalleDAO();
    private final EnrolementService enrolementService = new EnrolementService();

    private boolean saved = false;

    @FXML
    public void initialize() {
        try {
            chargerProfesseurs();
            chargerCours();
            chargerSalles();

            comboJour.setItems(FXCollections.observableArrayList(DayOfWeek.values()));
            comboJour.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(DayOfWeek d, boolean empty) {
                    super.updateItem(d, empty);
                    setText(empty || d == null ? null : jourFr(d));
                }
            });
            comboJour.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(DayOfWeek d, boolean empty) {
                    super.updateItem(d, empty);
                    setText(empty || d == null ? "Choisir..." : jourFr(d));
                }
            });
            comboJour.setValue(DayOfWeek.MONDAY);

            comboFrequence.setItems(FXCollections.observableArrayList(FrequenceCours.values()));
            comboFrequence.setValue(FrequenceCours.HEBDO);

            dateDebutSemestre.setValue(LocalDate.now());
            dateFinSemestre.setValue(LocalDate.now().plusMonths(4));

            int y = LocalDate.now().getYear();
            fieldAnneeAcademique.setText(y + "-" + (y + 1));
            fieldHeuresPrevues.setText("60");

        } catch (Exception e) {
            showErreur("Erreur initialisation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void chargerProfesseurs() {
        SessionContext ctx = SessionContext.getInstance();
        if (ctx.isProfesseur() && ctx.getUtilisateurConnecte() != null
                && ctx.getUtilisateurConnecte().getProfesseurLie() != null) {
            Professeur p = ctx.getUtilisateurConnecte().getProfesseurLie();
            comboProfesseur.setItems(FXCollections.observableArrayList(p));
            comboProfesseur.setValue(p);
            comboProfesseur.setDisable(true);
        } else {
            List<Professeur> profs = professeurDAO.findAllActifs();
            comboProfesseur.setItems(FXCollections.observableArrayList(profs));
            comboProfesseur.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Professeur p, boolean empty) {
                    super.updateItem(p, empty);
                    setText(empty || p == null ? null
                            : p.getNomComplet() + " (" + p.getMatricule() + ")");
                }
            });
            comboProfesseur.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Professeur p, boolean empty) {
                    super.updateItem(p, empty);
                    setText(empty || p == null ? "Choisir..." : p.getNomComplet());
                }
            });
        }
    }

    private void chargerCours() {
        List<Cours> cours = coursDAO.findAllCours();
        comboCours.setItems(FXCollections.observableArrayList(cours));
        comboCours.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Cours c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getCode() + " — " + c.getIntitule());
            }
        });
        comboCours.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Cours c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? "Choisir un cours..." : c.getCode() + " — " + c.getIntitule());
            }
        });
    }

    private void chargerSalles() {
        List<Salle> salles = salleDAO.findAllSalles();
        comboSalle.setItems(FXCollections.observableArrayList(salles));
        comboSalle.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Salle s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null : s.getNom() + " (" + s.getBatiment() + ")");
            }
        });
        comboSalle.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Salle s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? "Choisir une salle..." : s.getNom());
            }
        });
    }

    @FXML
    public void handleSauvegarder() {
        try {
            clearErreur();

            Professeur professeur = comboProfesseur.getValue();
            Cours      cours      = comboCours.getValue();
            Salle      salle      = comboSalle.getValue();

            if (professeur == null) throw new IllegalArgumentException("Choisissez un professeur.");
            if (cours == null)      throw new IllegalArgumentException("Choisissez un cours.");
            if (salle == null)      throw new IllegalArgumentException("Choisissez une salle.");

            String annee = fieldAnneeAcademique.getText() == null
                    ? "" : fieldAnneeAcademique.getText().trim();
            if (annee.isBlank())
                throw new IllegalArgumentException("Année académique obligatoire (ex: 2025-2026).");

            int heuresPrevues;
            try { heuresPrevues = Integer.parseInt(fieldHeuresPrevues.getText().trim()); }
            catch (Exception e) { throw new IllegalArgumentException("Heures prévues : entier requis."); }

            DayOfWeek      jour      = comboJour.getValue();
            FrequenceCours frequence = comboFrequence.getValue();
            if (jour == null)      throw new IllegalArgumentException("Choisissez un jour.");
            if (frequence == null) throw new IllegalArgumentException("Choisissez une fréquence.");

            LocalTime heureDebut = parseHeure(fieldHeureDebut.getText(), "Heure début");
            LocalTime heureFin   = parseHeure(fieldHeureFin.getText(),   "Heure fin");
            int dureeMinutes = (int) Duration.between(heureDebut, heureFin).toMinutes();
            if (dureeMinutes <= 0)
                throw new IllegalArgumentException("Heure fin doit être après heure début.");

            LocalDate debutSemestre = dateDebutSemestre.getValue();
            LocalDate finSemestre   = dateFinSemestre.getValue();
            if (debutSemestre == null || finSemestre == null)
                throw new IllegalArgumentException("Dates du semestre obligatoires.");
            if (finSemestre.isBefore(debutSemestre))
                throw new IllegalArgumentException("Date fin doit être >= date début.");

            PeriodiciteCours periodicite = new PeriodiciteCours();
            periodicite.setJourSemaine(jour);
            periodicite.setHeureDebut(heureDebut);
            periodicite.setHeureFin(heureFin);
            periodicite.setFrequence(frequence);
            periodicite.setDureeMinutes(dureeMinutes);

            Assignation result = enrolementService.assignerCours(
                    professeur, cours, salle,
                    annee, heuresPrevues,
                    periodicite, debutSemestre, finSemestre);

            System.out.println("✅ Assignation créée ID=" + result.getId());
            this.saved = true;
            closeWindow();

        } catch (Exception e) {
            showErreur(e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML public void handleAnnuler() { closeWindow(); }

    private LocalTime parseHeure(String txt, String label) {
        if (txt == null || txt.isBlank())
            throw new IllegalArgumentException(label + " obligatoire (format HH:mm).");
        try { return LocalTime.parse(txt.trim(), DateTimeFormatter.ofPattern("HH:mm")); }
        catch (Exception e) {
            throw new IllegalArgumentException(label + " invalide (attendu HH:mm, ex: 08:30).");
        }
    }

    private String jourFr(DayOfWeek d) {
        return switch (d) {
            case MONDAY    -> "Lundi";
            case TUESDAY   -> "Mardi";
            case WEDNESDAY -> "Mercredi";
            case THURSDAY  -> "Jeudi";
            case FRIDAY    -> "Vendredi";
            case SATURDAY  -> "Samedi";
            case SUNDAY    -> "Dimanche";
        };
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

    public boolean isSaved() { return saved; }
}