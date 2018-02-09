<?php
include "c_mySqlHandlingFunctions.php";

// test si le param�tre "operation" est pr�sent
if (isset($_REQUEST["operation"])) {
	try{
		$cnx = connexionPDO();
		switch($_REQUEST['operation']){
			//Cas de connection
			case "connection":
				print("Test de connection%");
				//On r�cup�re les donn�es
				$donnee = json_decode($lesdonnees);
				$username = $donnee[0];
				$mdp = $donnee[1];
				
				$req = $cnx->prepare("SELECT `comptable`, `id` FROM `visiteur` WHERE `login` = $username AND `mdp` = $mdp");
				$req->execute();
				

			  
				// s'il y a un profil, on r�cup�re le premier
				if ($ligne = $req->fetch(PDO::FETCH_ASSOC)){
					echo "Connection r�ussit avec le pseudonyme $username et le mot de passe $mdp.\n%";

					//On r�cup�re les donn�es
					while ($ligne = $req->fetch(PDO::FETCH_ASSOC)){
						$resultat[] = $ligne;
					}
					//On envoie les r�sultats de la requ�te
					print(json_encode($resultat));
				}else{
					echo "Echec de connection avec l'identifiant $username et le mdp $mdp.\n%";
				}
			
			//Cas de comptabilit�
			case "isComptable":
				print("V�rifie si le compte $login est un compte comptable.%");
					$cnx = connexionPDO();
					$req = $cnx->prepare("SELECT `comptable` FROM `visiteur` WHERE `login` = $login");
					$req->execute();
				  
					// On r�cup�re la cellule "comptable" en relation avec le compte
					while ($ligne = $req->fetch(PDO::FETCH_ASSOC)){
						$resultat[] = $ligne;
					}
					print(json_encode($resultat));
				break;
				
				
				//chargement des frais forfaitis�s
				case "chargementFrais":
					print("Chargement des frais forfaitis�s du mois.%");
					
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
				  
					// s'il y a une fiche de frais on la r�cup�re
					if ($ligne = $req->fetch(PDO::FETCH_ASSOC)){
						while ($ligne = $req->fetch(PDO::FETCH_ASSOC)){
							$resultat[] = $ligne;
						}
						print(json_encode($ligne));
					}
					break;
					
				case "chargementFraisHorsForfait" :
					print("Chargement des frais hors-forfaitis�s du mois.%");
					
					$donnee = json_decode($lesdonnees);
					$userId = $donnee[0];
					$mois = $donnee[1];
					
					$cnx = connexionPDO();
					$req = $cnx->prepare("SELECT `libelle`, `date`, `montant`"
										."FROM `lignefraishorsforfait`"
										."WHERE `idvisiteur` = $userId AND `mois` = $mois order by `date` desc");
					$req->execute();
				  
					// s'il y a une fiche de frais on la r�cup�re
					if ($ligne = $req->fetch(PDO::FETCH_ASSOC)){
						while ($ligne = $req->fetch(PDO::FETCH_ASSOC)){
							$resultat[] = $ligne;
						}
						print(json_encode($ligne));
					}
					break;
					
		}
	// capture d'erreur d'acc�s � la base de donn�es
	} catch (PDOException $e) {
		print "Erreur !" . $e->getMessage();
		die();
	}
}
			




		// enregistrement dans la table profil du profil re�u
			case "enreg" :
			
			// r�cup�ration des donn�es en post
			$lesdonnees = $_REQUEST["lesdonnees"] ;
			$donnee = json_decode($lesdonnees) ;
			$datemesure = $donnee[0] ;
			$poids = $donnee[1] ;
			$taille = $donnee[2] ;
			$age = $donnee[3] ;
			$sexe = $donnee[4] ;
			// insertion dans la base de donn�es
			try {
				print ("enreg%") ;
				$cnx = connexionPDO();
				$larequete = "insert into profil (datemesure, poids, taille, age, sexe)" ;
				$larequete .= " values (\"$datemesure\", $poids, $taille, $age, $sexe)" ;
				print ($larequete);
				$req = $cnx->prepare($larequete);
				$req->execute();
				
			// capture d'erreur d'acc�s � la base de donn�es
			} catch (PDOException $e) {
				print "Erreur !" . $e->getMessage();
				die();
			}
		// demande de r�cup�ration de tous les profils	
		}elseif ($_REQUEST["operation"]=="tous") {

			try {
				// cr�ation d'un curseur pour r�cup�rer les profils
				print("tous%");
				$cnx = connexionPDO();
				$req = $cnx->prepare("select * from profil order by datemesure desc");
				$req->execute();
			  
				// s'il y a un profil, on r�cup�re le premier
				while ($ligne = $req->fetch(PDO::FETCH_ASSOC)){
					$resultat[] = $ligne;
				}
				
				print(json_encode($resultat)) ;

			// capture d'erreur d'acc�s � la base de donn�es
			} catch (PDOException $e) {
				print "Erreur !" . $e->getMessage();
				die();
			}
			
		// demande de suppression d'un profil	
		}elseif ($_REQUEST["operation"]=="suppr") {
			
			// r�cup�ration des donn�es en post
			$lesdonnees = $_REQUEST["lesdonnees"] ;
			$donnee = json_decode($lesdonnees) ;
			$datemesure = $donnee[0] ;

			// suppresion dans la base de donn�es
			try {
				$cnx = connexionPDO();
				$larequete = "delete from profil where datemesure = '$datemesure'" ;
				print "suppr%".$larequete ;
				$req = $cnx->prepare($larequete);
				$req->execute();
				
			// capture d'erreur d'acc�s � la base de donn�es
			} catch (PDOException $e) {
				print "Erreur !" . $e->getMessage();
				die();
			}
			
		}

}

?>