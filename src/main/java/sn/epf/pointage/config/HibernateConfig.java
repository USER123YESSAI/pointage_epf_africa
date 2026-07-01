package sn.epf.pointage.config;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateConfig {

    private static SessionFactory sessionFactory;

    private HibernateConfig() {}

    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null || sessionFactory.isClosed()) {
            try {
                Configuration configuration = new Configuration();

                // DB configuration
                // - URL/user/pass peuvent être surchargés via variables d'environnement
                // - sinon, on utilise le hibernate.cfg.xml
                String envUrl = System.getenv("DB_URL");
                String envUser = System.getenv("DB_USER");
                String envPass = System.getenv("DB_PASS");

                configuration.configure("hibernate.cfg.xml");


                if (envUrl != null && !envUrl.isBlank()) {
                    configuration.setProperty("hibernate.connection.url", envUrl);
                }
                if (envUser != null && !envUser.isBlank()) {
                    configuration.setProperty("hibernate.connection.username", envUser);
                }
                if (envPass != null) {
                    configuration.setProperty("hibernate.connection.password", envPass);
                }

                sessionFactory = configuration.buildSessionFactory();
                System.out.println("✅ SessionFactory Hibernate initialisée avec succès.");
            } catch (Exception e) {
                // Ne pas afficher le mot de passe.
                System.err.println("❌ Erreur initialisation Hibernate : " + e.getMessage());
                throw new RuntimeException("Impossible d'initialiser Hibernate (vérifier la base MySQL/URL/port/accès)", e);
            }
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            System.out.println("SessionFactory fermée.");
        }
    }
}

