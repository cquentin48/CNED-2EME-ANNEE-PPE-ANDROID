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
								$req2 = $cnx->prepare('SELECT COUNT(*) as "nbFraisHF", `idvisiteur`,`mois`'
													 ."FROM `lignefraishorsforfait`"
													 ."WHERE `idvisiteur` = '$userId' AND `mois` = '$mois' AND `libelle` NOT LIKE '%REFUSE%'"
													 ."GROUP BY `idvisiteur`,`mois`;");
								$req2->execute();
								$ligne2 = $req2->fetchAll(PDO::FETCH_ASSOC);
								if(isset($ligne2[0]['nbFraisHF'])){
									$tabFrais[$key]['nbFraisHF'] = $ligne2[0]['nbFraisHF'];
								}
								if(!isset($tabFrais[$key]['nbFraisHF'])){
									$tabFrais[$key]['nbFraisHF'] = 0;
								}
								
								$req2 = $cnx->prepare('SELECT `id`, `libelle`, EXTRACT(DAY FROM `date`) as "jour",`montant`'
													."FROM `lignefraishorsforfait`"
													."WHERE `idvisiteur` = '$userId' AND `mois` = '$mois' AND `libelle` NOT LIKE '%REFUSE%';");
								$req2->execute();
								
								$ligne2 = $req2->fetchAll(PDO::FETCH_ASSOC);
								if(isset($ligne2)){
									for($i = 0;$i<sizeof($ligne2);$i++){
										$tabFrais[$key]['id'.$i] = $ligne2[$i]['id'];
										$tabFrais[$key]['libelle'.$i] = $ligne2[$i]['libelle'];
										$tabFrais[$key]['jour'.$i] = $ligne2[$i]['jour'];
										$tabFrais[$key]['montant'.$i] = $ligne2[$i]['montant'];
									}
								}
							}
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
						
						//On récupère les données
						$fraisForfaitIdTab = array(0 => 'ETP', 1 => 'NUI', 2 => 'KM', 3 => 'REP');
						$lesdonnees = $_REQUEST['lesdonnees'];
						$updateFicheFraisTab = json_decode($lesdonnees);
						$userId = $updateFicheFraisTab[0];
						echo "<pre>";
						print_r($updateFicheFraisTab);
						
						//Suppression du premier élément du tableau de fiche
						unset($updateFicheFraisTab[0]);
						foreach($updateFicheFraisTab as $uneFiche){
							//Parcours des cas de modification
							switch($uneFiche[6]){
								//Cas de modification
								case "MODIFIE":
									for($i=0;$i<sizeof($fraisForfaitIdTab);$i++){
										$montant = $uneFiche[$i+2];
										$fraisForfait = $fraisForfaitIdTab[$i];
										$requete = "UPDATE `lignefraisforfait`"
													 ." SET `quantite` = $montant"
													 ." WHERE `mois` ='$uneFiche[0]$uneFiche[1]' AND `idvisiteur` = '$userId' AND `idfraisforfait` = '$fraisForfait';";
										print($requete."%");
										$req = $cnx->prepare($requete);
										print("Information : Fiche du mois de $uneFiche[0]$uneFiche[1] mise à jour.%");
										$req->execute();
									}
									
									//S'il y a présence de frais hors-forfait dans la fiche du mois
									if(isset($uneFiche[7])){
										//Parcours des frais Hors-Forfait
										foreach($uneFiche[7] as $unFraisHF){
											switch($unFraisHF[3]){
												//Cas de modification
												case "MODIFIE":
													$requete = "UPDATE `lignefraishorsforfait`"
																 ." SET `libelle` = '$unFraisHF[1]', `montant` = $unFraisHF[0]"
																 ." WHERE `idvisiteur` = '$userId' AND `mois` = '$uneFiche[0]$uneFiche[1]' AND `id` = $unFraisHF[2]";
													$req = $cnx->prepare($requete);
													print ($requete."%");
													$req->execute();
													
													print("Information : Frais Hors-Forfait du mois de $uneFiche[0]$uneFiche[1] au jour $unFraisHF[3] mis à jour.%");
												break;
												
												//Cas de création
												case "CREE":
													$mois = $uneFiche[1];
													$annee = $uneFiche[0];
													$jour = $unFraisHF[3];
													$date = $annee."-".$mois."-".$jour;
													
													$req = $cnx->prepare("INSERT INTO `lignefraishorsforfait`"
																 ."(`id`, `idVisiteur`, `mois`, `libelle`, `date`, `quantite`)"
																 ."VALUES ($unFraisHF[4], $idVisiteur, $uneFiche[1], $unFraisHF[2], date, montant)");
													$req->execute();
													
													print("Information : Frais Hors-Forfait du mois de $uneFiche[0]$uneFiche[1] au jour $unFraisHF[3] inséré dans la base de donnée MYSQL.%");
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
									$montantValide = 0;
									for($i = 0;$i<sizeof(fraisForfaitIdTab);$i++){
										$montantValide = $fraisForfaitMontantTab[$i]*$uneFiche[$i];
									}
									$req->prepare("INSERT INTO `fichefrais` (`idVisiteur`, `mois`, `nbjustificatifs`, `montantValide`, `datemodif`, `idetat`)"
												 ."VALUES ($userId, $uneFiche[1], 0, $montantValide, NOW(), 'CR');");
									$req->execute();
									for($i=0;$i<sizeof($fraisForfaitIdTab);$i++){
										$montant = $uneFiche[$i+2];
										$fraisForfait = $fraisForfaitIdTab[$i];
										$requete = "INSERT INTO `lignefraisforfait`(`idvisiteur`,`mois`,`idfraisforfait`,`quantite`)"
													 ."VALUES('$userId','$uneFiche[1]','$fraisForfait', $quantite);";
										print($requete);
										$req->prepare($requete);
										//$req->execute();
										print("Information : Fiche du mois de $uneFiche[0]$uneFiche[1] insérée dans la base de donnée MYSQL.%");
									}
									
									//S'il y a présence de frais hors-forfait dans la fiche du mois
									if(isset($uneFiche[7])){
										//Parcours des frais Hors-Forfait
										foreach($uneFiche[7] as $unFraisHF){
											switch($unFrais[7]){
												//Cas de modification
												case "MODIFIE":
													$req->prepare("UPDATE `lignefraishorsforfait`"
																 ."SET `libelle` = '$unFraisHF[2]', `montant` = $unFraisHF[1]");
													$req->execute();
													print("Information : Frais Hors-Forfait du mois de $uneFiche[0]$uneFiche[1] au jour de $unFraisHF[3] mise à jour.%");
												break;
												
												//Cas de création
												case "CREE":
													$mois = $uneFiche[1];
													$annee = $uneFiche[0];
													$jour = $unFraisHF[3];
													$date = $annee."-".$mois."-".$jour;
													
													$req->prepare("INSERT INTO `lignefraishorsforfait`"
																 ."(`id`, `idVisiteur`, `mois`, `libelle`, `date`, `montant`)"
																 ."VALUES ('$unFraisHF[4]', '$idVisiteur', '$uneFiche[0]$uneFiche[1]', '$unFraisHF[2]', '$date', montant)");
													$req->execute();
													print("Information : Frais Hors-Forfait du mois de $uneFiche[0]$uneFiche[1] au jour de $unFraisHF[3] insérée dans la base de donnée MYSQL.%");
												break;
												
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
											echo "<pre>";
											print_r($unFraisHF);
											switch($unFraisHF[4]){
												//Cas de modification
												case "MODIFIE":
													$req = $cnx->prepare("UPDATE `lignefraishorsforfait`"
																 ."SET `libelle` = '$unFraisHF[2]', `montant` = $unFraisHF[1]"
																 ."WHERE `idvisiteur` = '$userId' AND `mois` = '$uneFiche[0]$uneFiche[1]'");
													$req->execute();
													
													print("Information : Frais Hors-Forfait du mois de $uneFiche[0]$uneFiche[1] au jour $unFraisHF[3] mis à jour.%");
												break;
												
												//Cas de création
												case "CREE":
													$mois = $uneFiche[1];
													$annee = $uneFiche[0];
													$jour = $unFraisHF[3];
													$date = $annee."-".$mois."-".$jour;
													
													$req = $cnx->prepare("INSERT INTO `lignefraishorsforfait`"
																 ."(id, idVisiteur, mois, libelle, date, montant)"
																 ."VALUES ('$unFraisHF[4]', '$idVisiteur', '$uneFiche[1]', '$unFraisHF[2]', '$date', montant)");
													$req->execute();
													
													print("Information : Frais Hors-Forfait du mois de $uneFiche[0]$uneFiche[1] au jour $unFraisHF[3] inséré dans la base de donnée MYSQL.%");
												break;
												
												default:
													print("Avertissement : aucun cas de modification/création du frais hors-forfait. Passage au frais suivant.%");
												break;
											}
										}
									}
								break;
							}
						}
					break;
							
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