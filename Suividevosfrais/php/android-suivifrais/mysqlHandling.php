<?php
require_once('c_mySqlHandlingFunctions.php');
// test si le paramètre "operation" est présent
if (isset($_POST["operation"])) {
	$cnx = connexionPDO();
	try{
		switch($_POST['operation']){
			//Cas de connection
			case "connexion":
				print("connection%");
				print("connection%");
				//On récupère les données
				$lesdonnees = $_REQUEST['lesdonnees'];
				$donnee = json_decode($lesdonnees);
				$username = $donnee[0];
				$mdp = $donnee[1];
				$mdpMysql = hash('sha256',$mdp);
				
				$req = $cnx->prepare("SELECT `comptable`, `id`, `login`"
									." FROM `visiteur` "
									." WHERE `login` = '$username' AND `mdp` = '$mdpMysql'"
									);
				$req->execute();
				$ligne = $req->fetch(PDO::FETCH_ASSOC);
				// s'il y a un profil, on récupère le premier
				if(isset($ligne['comptable'])){
					echo "succes%";
					echo "Connection réussit avec le pseudonyme $username et le mot de passe $mdp.%";

					//On envoie les résultats de la requête
					print(json_encode($ligne));
				}else{
					echo "echec%";
					echo "Echec de connection avec l'identifiant '$username' et le mdp '$mdp'.%";
				}			
				break;
				
				//chargement des frais forfaitisés
				case "chargementFrais":
					print("chargementFrais%");
					print("chargementFrais%");
					
					//On récupère les données
					$lesdonnees = $_REQUEST['lesdonnees'];
					$donnee = json_decode($lesdonnees);
					$userId = $donnee[0];
					print("Chargement des frais forfaitisés du visiteur ".$userId.".%");
					
					$req = $cnx->prepare("SELECT `mois`"
										."FROM `fichefrais`"
										."WHERE `idvisiteur` = '$userId'");
					$req->execute();
					$ligne = $req->fetchAll(PDO::FETCH_ASSOC);

					// s'il y a une fiche de frais on la récupère
					if(isset($ligne)){
							echo "succes%";
							foreach($ligne as $key=>$uneLigne){
								//On ajoute le mois dans le tableau de la fiche
								$tabFrais[$key]['mois'] = $uneLigne['mois'];
								
								//Variable pour la requête SQL
								$mois = $tabFrais[$key]['mois'];
								
								//Requête pour l'import des frais forfaitisés
								$req2 = $cnx->prepare("SELECT `quantite`,`idfraisforfait`"
													 ."FROM `lignefraisforfait`"
													 ."WHERE `idvisiteur` = '$userId' AND `mois` = '$mois'");
								
								//Exécution de la requête
								$req2->execute();
								
								//Récupération des données
								$ligne2 = $req2->fetchAll(PDO::FETCH_ASSOC);
								
								//En cas de résultat
								if(isset($ligne2)){
									//Copie des données dans le tableau
									for($i = 0;$i<sizeof($ligne2);$i++){
										//Parcours des frais forfaitisés
										switch($ligne2[$i]['idfraisforfait']){
											//Nuitée
											case "ETP":
												$tabFrais[$key]['etp'] = $ligne2[$i]['quantite'];
											break;
											//Nuitée
											case "KM":
												$tabFrais[$key]['km'] = $ligne2[$i]['quantite'];
											break;
											//Nuitée
											case "NUI":
												$tabFrais[$key]['nui'] = $ligne2[$i]['quantite'];
											break;
											//Nuitée
											case "REP":
												$tabFrais[$key]['rep'] = $ligne2[$i]['quantite'];
											break;
										}
									}
								}
								//requête sql d'import des frais Hors-Forfait
								$requete = 'SELECT COUNT(*) as "nbFraisHF", `idvisiteur`,`mois`'
													 ."FROM `lignefraishorsforfait`"
													 ."WHERE `idvisiteur` = '$userId' AND `mois` = '$mois' AND `libelle` NOT LIKE '%REFUSE%'"
													 ."GROUP BY `idvisiteur`,`mois`;";
								
								//Préparation de la requête
								$req2 = $cnx->prepare($requete);
								
								//Exécution de la requ
								$req2->execute();
								
								//Copie du résultat dans la variable $ligne2
								$ligne2 = $req2->fetchAll(PDO::FETCH_ASSOC);
								
								//En cas de présence de frais hf
								if(isset($ligne2[0]['nbFraisHF'])){
									//Copie du nombre de frais HF dans la fiche du mois
									$tabFrais[$key]['nbFraisHF'] = $ligne2[0]['nbFraisHF'];
								}
								//Sinon
								if(!isset($tabFrais[$key]['nbFraisHF'])){
									//Ajout du nombre 0 dans la fiche de frais pour indiquer l'absence de frais HF
									$tabFrais[$key]['nbFraisHF'] = 0;
								}
								
								//Requête
								$requete = 'SELECT `id`, `libelle`, EXTRACT(DAY FROM `date`) as "jour",`montant`'
													."FROM `lignefraishorsforfait`"
													."WHERE `idvisiteur` = '$userId' AND `mois` = '$mois' AND `libelle` NOT LIKE '%REFUSE%';";

								//Préparation de la requête
								$req2 = $cnx->prepare($requete);
								
								//Exécution de la requête
								$req2->execute();
								
								//Copie du résultat dans la variable "ligne2"
								$ligne2 = $req2->fetchAll(PDO::FETCH_ASSOC);
								
								//En cas de retour
								if(isset($ligne2)){
									//Ajout des frais hors-forfait à la fiche de frais du mois correspondant
									for($i = 0;$i<sizeof($ligne2);$i++){
										$tabFrais[$key]['id'.$i] = $ligne2[$i]['id']; //Identifiant
										$tabFrais[$key]['libelle'.$i] = $ligne2[$i]['libelle']; //Libellé
										$tabFrais[$key]['jour'.$i] = $ligne2[$i]['jour']; //Jour
										$tabFrais[$key]['montant'.$i] = $ligne2[$i]['montant']; //Montant
									}
								}
							}
							
							//Envoi de la variable du tableau de frais dans la requête SQL
							print(json_encode($tabFrais, JSON_UNESCAPED_UNICODE));
							print "%";
					}else{
						echo "echec%";
						echo "Aucun élément chargé%";
					}
					break;
					
					//Mise à jour des frais
					case "majFrais":
						print("majFrais%");
						print("majFrais%");
						
						//Tables de références pour les frais forfaitisés
						$fraisForfaitIdTab = array(0 => 'ETP', 1 => 'NUI', 2 => 'KM', 3 => 'REP');
						$fraisForfaitMontantTab = array(0 => 110, 1 => 0.62, 2 => 80, 3 => 25);
						
						//Récupération des données à partir de celles envoyées depuis le smartphone
						$lesdonnees = $_REQUEST['lesdonnees'];
						
						//Décodage du tableau JSON
						$updateFicheFraisTab = json_decode($lesdonnees);
						
						//Récupération de l'index utilisateur
						$userId = $updateFicheFraisTab[0];
						
						//Suppression du premier élément du tableau de fiche
						unset($updateFicheFraisTab[0]);
						
						//Parcours des fiches de frais
						foreach($updateFicheFraisTab as $uneFiche){
							//Récupération du mois et de l'année de la fiche
							$mois = $uneFiche[1];			
							$annee = $uneFiche[0];
							
							//Création de la clé du mois pour la requête
							if($mois<10){
								$key = $annee.'0'.$mois;
								}else{
								$key = $annee.$mois;
							}
						
							//Parcours des cas de modification
							switch($uneFiche[6]){
								//Cas de modification
								case "MODIFIE":
									for($i=0;$i<sizeof($fraisForfaitIdTab);$i++){
										$montant = $uneFiche[$i+2];
										$fraisForfait = $fraisForfaitIdTab[$i];
										
										//Requête SQL
										$requete = "UPDATE `lignefraisforfait`"
												  ." SET `quantite` = $montant"
												  ." WHERE `mois` ='$key' AND `idvisiteur` = '$userId' AND `idfraisforfait` = '$fraisForfait';";
													 
										//Préparation de la requête SQL
										$req = $cnx->prepare($requete);
										
										//Exécution de la requête SQL
										$req->execute();
										
										//Affichage de la requête dans la console
										print("SQL : ".$requete."%");
										
										//Affichage du résultat de l'opération dans la console
										print("Information : Fiche du mois de $key mise à jour.%");
									}
									
									//S'il y a présence de frais hors-forfait dans la fiche du mois
									if(isset($uneFiche[7])){
										//Parcours des frais Hors-Forfait
										foreach($uneFiche[7] as $unFraisHF){
											switch($unFraisHF[3]){
												//Cas de modification
												case "MODIFIE":
												
													//Requête sql
													$requete = "UPDATE `lignefraishorsforfait`"
																 ." SET `libelle` = '$unFraisHF[1]', `montant` = $unFraisHF[0]"
																 ." WHERE `idvisiteur` = '$userId' AND `mois` = '$key' AND `id` = $unFraisHF[2]";
																 
													//Préparation de la requête SQL
													$req = $cnx->prepare($requete);
													
													//Exécution de la requête SQL
													$req->execute();
													
													//Affichage de la requête dans la console
													print("SQL : ".$requete."%");
													
													//Affichage du résultat de l'opération dans la console
													print("Information : Frais Hors-Forfait du mois de $key au jour $unFraisHF[3] mis à jour.%");
												break;
												
												//Cas de création
												case "CREE":
													//Récupération de la date à partir de la fiche et du frais hors-forfait(jour)
													$mois = $uneFiche[1];
													$annee = $uneFiche[0];
													$jour = $unFraisHF[3];
													
													//Création de la date au format YYYY-MM-DD
													if($mois<10){
														$date = $annee."-"."0".$mois."-".$jour;
													}else{
														$date = $annee."-".$mois."-".$jour;
													}
													
													$requete = "INSERT INTO `lignefraishorsforfait`"
																 ."(`id`, `idVisiteur`, `mois`, `libelle`, `date`, `quantite`)"
																 ."VALUES ($unFraisHF[4], $idVisiteur, $key, $unFraisHF[2], date, montant)";
													
													$req = $cnx->prepare($requete);
													$req->execute();
													
													//Affichage de la requête sql dans la console
													echo "SQL : ".$requete."%";
													
													//Affichage du résultat de l'opération dans la console
													print("Information : Frais Hors-Forfait du mois de $key au jour $unFraisHF[3] inséré dans la base de donnée MYSQL.%");
												break;
												
												default:
													print("Avertissement : aucun cas de modification/création du frais hors-forfait. Passage au frais suivant.%");
												break;
											}
										}
									}
								break;
								
								//Cas de création
								case "CREE":
								
									//Récupération du mois et de l'année de la fiche
									$annee = $uneFiche[0];
									$mois = $uneFiche[1];
									
									//Création du montant valide pour la requête SQL
									$montantValide = 0;
									for($i = 0;$i<sizeof($fraisForfaitIdTab);$i++){
										$montantValide = $fraisForfaitMontantTab[$i]*$uneFiche[$i+2];
									}
									
									//Ecriture de la requête SQL pour la création de la fiche
									$requete = "INSERT INTO `fichefrais` (`idVisiteur`, `mois`, `nbjustificatifs`, `montantValide`, `datemodif`, `idetat`)"
												 ."VALUES ('$userId', '$key', 0, $montantValide, NOW(), 'CR');";
												 
									//Affichage de la requête pour la console
									echo "SQL : ".$requete."%";
									
									//Préparation de la requête
									$req = $cnx->prepare($requete);
									
									//Exécution de la requête
									$req->execute();
									
									//Insertion des frais forfaitisés dans la base de données sql
									for($i=0;$i<sizeof($fraisForfaitIdTab);$i++){
										//Récupération du montant
										$montant = $uneFiche[$i+2];
										
										//Récupération du type de frais forfaitisé
										$fraisForfait = $fraisForfaitIdTab[$i];
										
										//SQL
										$requete = "INSERT INTO `lignefraisforfait`(`idvisiteur`,`mois`,`idfraisforfait`,`quantite`)"
													 ."VALUES('$userId','$key','$fraisForfait', $montant);\n";
										
										//Préparation de la requête SQL
										$req = $cnx->prepare($requete);
										
										//Exécution de la requête SQL
										$req->execute();
										
										//Affichage de la requête dans la console
										echo "SQL : ".$requete."%";
										
										//Affichage du résultat dans la console
										print("Information : Fiche du mois de $key insérée dans la base de donnée MYSQL.%");
									}
									
									//S'il y a présence de frais hors-forfait dans la fiche du mois
									if(isset($uneFiche[7])){
										//Parcours des frais Hors-Forfait
										foreach($uneFiche[7] as $unFraisHF){
											
											//Parcours du type de modification du frais HF
											switch($unFraisHF[5]){
												//Cas de modification
												case "MODIFIE":
													//Ecriture de la requête
													$requete = "UPDATE `lignefraishorsforfait`"
																 ."SET `libelle` = '$unFraisHF[2]', `montant` = $unFraisHF[1]";
																 
													//Préparation de la requête			 
													$req->prepare($requete);
													
													//Exécution de la requête
													$req->execute();
													
													//Affichage de la requête dans la console
													echo "SQL : ".$requete."%";
													
													//Affichage du résultat de l'opération dans la console
													print("Information : Frais Hors-Forfait du mois de $key au jour de $unFraisHF[3] mise à jour.%");
												break;
												
												//Cas de création
												case "CREE":
												
													//Récupération de la date à partir de la fiche et du frais hors-forfait(jour)
													$mois = $uneFiche[1];
													$annee = $uneFiche[0];
													$jour = $unFraisHF[3];
													
													//Création de la date au format YYYY-MM-DD
													if($mois<10){
														$date = $annee."-"."0".$mois."-".$jour;
													}else{
														$date = $annee."-".$mois."-".$jour;
													}
													
													//Ecriture de la requête SQL
													$requete = "INSERT INTO `lignefraishorsforfait`"
																 ."(`id`, `idVisiteur`, `mois`, `libelle`, `date`, `montant`)"
																 ."VALUES ('$unFraisHF[4]', '$idVisiteur', '$key', '$unFraisHF[2]', '$date', montant)";
													
													//Préparation de la requête SQL
													$req = $cnx->prepare($requete);
																 
													//Exécution de la requête
													$req = $cnx->execute();
													
													//Affichage de la requête dans la console
													echo "SQL : ".$requete."%";
													
													//Affichage du résultat de l'opération dans la console
													print("Information : Frais Hors-Forfait du mois de $key au jour de $unFraisHF[3] insérée dans la base de donnée MYSQL.%");
												break;
												
												//Cas d'erreur
												default:
													print("Avertissement : Aucun cas de modification/création du frais hors-forfait. Passage au frais suivant.%");
												break;
											}
										}
									}
							break;
								
								//Sinon
								default:
									//S'il y a présence de frais hors-forfait dans la fiche du mois
									if(isset($uneFiche[7])){
										//Parcours des frais Hors-Forfait
										foreach($uneFiche[7] as $unFraisHF){
											switch($unFraisHF[4]){
												//Cas de modification
												case "MODIFIE":
												
													//SQL
													$requete = "UPDATE `lignefraishorsforfait`"
																 ."SET `libelle` = '$unFraisHF[2]', `montant` = $unFraisHF[1]"
																 ."WHERE `idvisiteur` = '$userId' AND `mois` = $key";
																 
													//Préparation de la requête SQL
													$req = $cnx->prepare($requete);
													
													//Exécution de la requête SQL
													$req->execute();
													
													//Affichage de la requête dans la console
													echo "SQL : ".$requete."%";
													
													//Affichage du résultat de l'opération dans la console
													print("Information : Frais Hors-Forfait du mois de $key au jour de $unFraisHF[3] mis à jour.%");
												break;
												
												//Cas de création
												case "CREE":
													//Récupération de la date à partir de la fiche de frais et du frais hors-forfait correspondant
													$mois = $uneFiche[1];
													$annee = $uneFiche[0];
													$jour = $unFraisHF[3];
													
													//Ecriture de la date au format YYYY-MM-DD
													if($mois<10){
														$date = $annee."-"."0".$mois."-".$jour;
													}else{
														$date = $annee."-".$mois."-".$jour;
													}
													
													//SQL
													$requete = "INSERT INTO `lignefraishorsforfait`"
																 ."(id, idVisiteur, mois, libelle, date, montant)"
																 ."VALUES ('$unFraisHF[4]', '$idVisiteur', '$key', '$unFraisHF[2]', '$date', montant)";
													
													//Préparation de la requête
													$req = $cnx->prepare($requete);
													
													//Exécution de la requête
													$req->execute();
													
													//Affichage de la requête dans la console
													echo "SQL : ".$requete."%";
													
													//Affichage du résultat de l'opération dans la console
													print("Information : Frais Hors-Forfait du mois de $key au jour $unFraisHF[3] inséré dans la base de donnée MYSQL.%");
												break;
												
												default:
													//En cas d'absence de création/modification de la fiche => Affichage console
													print("Avertissement : aucun cas de modification/création du frais hors-forfait. Passage au frais suivant.%");
												break;
											}
										}
									}
								break;
							}
						}
					break;
						
					//Absence d'opération
					default:
					break;
		}
	// capture d'erreur d'accès à la base de données
	} catch (PDOException $e) {
		print "Erreur !" . $e->getMessage();
		die();
	}
}
?>