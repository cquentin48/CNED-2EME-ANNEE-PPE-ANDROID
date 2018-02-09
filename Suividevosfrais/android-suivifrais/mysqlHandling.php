<?php
include "c_mySqlHandlingFunctions.php";

// test si le paramètre "operation" est présent
if (isset($_REQUEST["operation"])) {
	try{
		$cnx = connexionPDO();
		switch($_REQUEST['operation']){
			//Cas de connection
			case "connection":
				print("Test de connection%");
				//On récupère les données
				$donnee = json_decode($lesdonnees);
				$username = $donnee[0];
				$mdp = $donnee[1];
				
				$req = $cnx->prepare("SELECT `comptable`, `id` FROM `visiteur` WHERE `login` = $username AND `mdp` = $mdp");
				$req->execute();
				

			  
				// s'il y a un profil, on récupère le premier
				if ($ligne = $req->fetch(PDO::FETCH_ASSOC)){
					echo "Connection réussit avec le pseudonyme $username et le mot de passe $mdp.\n%";

					//On récupère les données
					while ($ligne = $req->fetch(PDO::FETCH_ASSOC)){
						$resultat[] = $ligne;
					}
					//On envoie les résultats de la requête
					print(json_encode($resultat));
				}else{
					echo "Echec de connection avec l'identifiant $username et le mdp $mdp.\n%";
				}
			
			//Cas de comptabilité
			case "isComptable":
				print("Vérifie si le compte $login est un compte comptable.%");
					$cnx = connexionPDO();
					$req = $cnx->prepare("SELECT `comptable` FROM `visiteur` WHERE `login` = $login");
					$req->execute();
				  
					// On récupère la cellule "comptable" en relation avec le compte
					while ($ligne = $req->fetch(PDO::FETCH_ASSOC)){
						$resultat[] = $ligne;
					}
					print(json_encode($resultat));
				break;
				
				
				//chargement des frais forfaitisés
				case "chargementFrais":
					print("Chargement des frais forfaitisés du mois.%");
					
					$donnee = json_decode($lesdonnees);
					$userId = $donnee[0];
					$mois = $donnee[1];
					
					$cnx = connexionPDO();
					$req = $cnx->prepare("SELECT *"
										."FROM `fichefrais` inner join `lignefraisforfait`"
										."on `fichefrais`.`idvisiteur` = `lignefraisforfait`.`idvisiteur`"
										."and `fichefrais`.`mois` = `lignefraisforfait`.`mois`"
										."WHERE `idvisiteur` = $userId AND `mois` = $mois order by `idfraisforfait` asc");
					$req->execute();
				  
					// s'il y a une fiche de frais on la récupère
					if ($ligne = $req->fetch(PDO::FETCH_ASSOC)){
						while ($ligne = $req->fetch(PDO::FETCH_ASSOC)){
							$resultat[] = $ligne;
						}
						print(json_encode($ligne));
					}
					break;
					
				case "chargementFraisHorsForfait" :
					print("Chargement des frais hors-forfaitisés du mois.%");
					
					$donnee = json_decode($lesdonnees);
					$userId = $donnee[0];
					$mois = $donnee[1];
					
					$cnx = connexionPDO();
					$req = $cnx->prepare("SELECT `libelle`, `date`, `montant`"
										."FROM `lignefraishorsforfait`"
										."WHERE `idvisiteur` = $userId AND `mois` = $mois order by `date` desc");
					$req->execute();
				  
					// s'il y a une fiche de frais on la récupère
					if ($ligne = $req->fetch(PDO::FETCH_ASSOC)){
						while ($ligne = $req->fetch(PDO::FETCH_ASSOC)){
							$resultat[] = $ligne;
						}
						print(json_encode($ligne));
					}
					break;
					
		}
	// capture d'erreur d'accès à la base de données
	} catch (PDOException $e) {
		print "Erreur !" . $e->getMessage();
		die();
	}
}
			




		// enregistrement dans la table profil du profil reçu
			case "enreg" :
			
			// récupération des données en post
			$lesdonnees = $_REQUEST["lesdonnees"] ;
			$donnee = json_decode($lesdonnees) ;
			$datemesure = $donnee[0] ;
			$poids = $donnee[1] ;
			$taille = $donnee[2] ;
			$age = $donnee[3] ;
			$sexe = $donnee[4] ;
			// insertion dans la base de données
			try {
				print ("enreg%") ;
				$cnx = connexionPDO();
				$larequete = "insert into profil (datemesure, poids, taille, age, sexe)" ;
				$larequete .= " values (\"$datemesure\", $poids, $taille, $age, $sexe)" ;
				print ($larequete);
				$req = $cnx->prepare($larequete);
				$req->execute();
				
			// capture d'erreur d'accès à la base de données
			} catch (PDOException $e) {
				print "Erreur !" . $e->getMessage();
				die();
			}
		// demande de récupération de tous les profils	
		}elseif ($_REQUEST["operation"]=="tous") {

			try {
				// création d'un curseur pour récupérer les profils
				print("tous%");
				$cnx = connexionPDO();
				$req = $cnx->prepare("select * from profil order by datemesure desc");
				$req->execute();
			  
				// s'il y a un profil, on récupère le premier
				while ($ligne = $req->fetch(PDO::FETCH_ASSOC)){
					$resultat[] = $ligne;
				}
				
				print(json_encode($resultat)) ;

			// capture d'erreur d'accès à la base de données
			} catch (PDOException $e) {
				print "Erreur !" . $e->getMessage();
				die();
			}
			
		// demande de suppression d'un profil	
		}elseif ($_REQUEST["operation"]=="suppr") {
			
			// récupération des données en post
			$lesdonnees = $_REQUEST["lesdonnees"] ;
			$donnee = json_decode($lesdonnees) ;
			$datemesure = $donnee[0] ;

			// suppresion dans la base de données
			try {
				$cnx = connexionPDO();
				$larequete = "delete from profil where datemesure = '$datemesure'" ;
				print "suppr%".$larequete ;
				$req = $cnx->prepare($larequete);
				$req->execute();
				
			// capture d'erreur d'accès à la base de données
			} catch (PDOException $e) {
				print "Erreur !" . $e->getMessage();
				die();
			}
			
		}

}

?>