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
					
					$fraisForfaitises;
					$fraisHF;
					
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
									for($i = 0; $i<sizeof($ligne2);$i++){
										switch($ligne2[$i]['idfraisforfait']){
											//Frais Kilométrique
											case "KM":
												$tabFrais[$key]['km'] = $ligne2[$i]['quantite'];
											break;
											
											//repas
											case "REP":
												$tabFrais[$key]['rep'] = $ligne2[$i]['quantite'];
											break;
											
											//nuitee
											case "NUI":
												$tabFrais[$key]['nui'] = $ligne2[$i]['quantite'];
											break;
											
											//etapes
											case "ETP":
												$tabFrais[$key]['etp'] = $ligne2[$i]['quantite'];
											break;
										}
									}
								}
								$req2 = $cnx->prepare('SELECT `mois`, count(*) as "nbFraisHF"'
													."FROM `lignefraishorsforfait`"
													."WHERE `idvisiteur` = '$userId' AND `mois` = '$mois' AND `libelle` NOT LIKE '%REFUSE%'"
													."GROUP By `mois`");
								$req2->execute();
								
								$ligne2 = $req2->fetchAll(PDO::FETCH_ASSOC);
								if(isset($ligne2)){
										if(isset($ligne2[0]['nbFraisHF'])){
											$tabFrais[$key]['nbFraisHF'] = $ligne2[0]['nbFraisHF'];
										}else{
											$tabFrais[$key]['nbFraisHF'] = 0;
										}
								}
								$req2 = $cnx->prepare('SELECT `id`, `libelle`, EXTRACT(DAY FROM `date`) as "jour",`montant`'
													."FROM `lignefraishorsforfait`"
													."WHERE `idvisiteur` = '$userId' AND `mois` = '$mois' AND `libelle` NOT LIKE '%REFUSE%';");
								$req2->execute();
								
								$ligne2 = $req2->fetchAll(PDO::FETCH_ASSOC);
								if(isset($ligne2)){
									for($i = 0; $i<sizeof($ligne2);$i++){
										$tabFrais[$key]['id'.$i] = $ligne2[$i]['id'];
										$tabFrais[$key]['jour'.$i] = $ligne2[$i]['jour'];
										$tabFrais[$key]['libelle'.$i] = $ligne2[$i]['libelle'];
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
					
					//Suppresion de frais hors-forfait
					case "deleteFraisHF":
						print("deleteFraisHF%");
						print("deleteFraisHF%");
						
						//On récupère les données
						$lesdonnees = $_REQUEST['lesdonnees'];
						$donnee = json_decode($lesdonnees);
						
						$fraisHFKey = $donnee[0];
						$req = $cnx->prepare("DELETE FROM lignefraishorsforfait"
											."WHERE `id` = $fraisHFKey");
						$req->execute();
						print("Frais Hors-forfait supprimé.%");
					break;
					
					case "mySQLSetFraisForfaitisee":
						print("mySQLSetFraisForfaitisee%");
						print("mySQLSetFraisForfaitisee%");
						
						//On récupère le tableau JSON
						$lesdonnees = $_REQUEST['lesdonnees'];
						$donnee = json_decode($lesdonnees);
						
						//On récupère les données
						$mois = $donnee[0];
						$libelle = $donnee[1];
						$idVisiteur = $donnee[2];
						$montant = $donnee[3];
						
						//On vérifie si la donnée existe ou pas
						$req = $cnx->prepare("SELECT `idvisiteur`, `mois`".
											 "FROM `lignefraisforfait`".
											 "WHERE `mois` = '$mois' AND `idvisiteur` = '$idVisiteur' AND `libelle` = '$libelle");
						$req->execute();
				
						$ligne = $req->fetch(PDO::FETCH_ASSOC);
						
						//Si le frais existe déjà => Passage par la requête UPDATE
						if(isset($ligne['idvisiteur'])){
							print("Frais déjà existant : mise à jour du frais");
							$req2 = $cnx->prepare("UPDATE `lignefraisforfait`".
												 "SET `quantite` = $montant".
												 "WHERE `mois` = '$mois' AND `idvisiteur` = '$idVisiteur' AND `libelle` = '$libelle");
							$req2->execute();
							print("Frais mis à jour!%");
						}
						//Si le frais n'existe pas => Passage par la requête INSERT
						else{
							print("Frais inexistant : insertion du frais");
							$req2 = $cnx->prepare("INSERT INTO `lignefraisforfait`".
												 "(`idvisiteur`,`mois`,`idfraisforfait`,`quantite`)".
												 "VALUES('$idVisiteur','$mois','$libelle','$montant');");
							$req2->execute();
							print("Frais ajouté%");
						}
					break;

					case "mySQLSetFraisHorsForfait":
						print("mySQLSetFraisHorsForfait%");
						print("mySQLSetFraisHorsForfait%");
						
						//On récupère le tableau JSON
						$lesdonnees = $_REQUEST['lesdonnees'];
						$donnee = json_decode($lesdonnees);
						
						//On récupère les données
						$id = $donnee[0];
						$mois = $donnee[1];
						$idVisiteur = $donnee[2];
						$libelle = $donnee[3];
						$date = $donnee[4];
						$montant = $donnee[5];
						
						//On vérifie si la donnée existe ou pas
						$req = $cnx->prepare("SELECT `idvisiteur`, `mois`".
											 "FROM `lignefraisforfait`".
											 "WHERE `mois` = '$mois' AND `idvisiteur` = '$idVisiteur' AND `libelle` = '$libelle");
						$req->execute();
				
						$ligne = $req->fetch(PDO::FETCH_ASSOC);
						
						//Si le frais existe déjà => Passage par la requête UPDATE
						if(isset($ligne['idvisiteur'])){
							print("Frais déjà existant : mise à jour du frais");
							$req2 = $cnx->prepare("UPDATE `lignefraisforfait`".
												 "SET `quantite` = $montant".
												 "WHERE `mois` = '$mois' AND `idvisiteur` = '$idVisiteur' AND `libelle` = '$libelle");
							$req2->execute();
							print("Frais mis à jour!%");
						}
						//Si le frais n'existe pas => Passage par la requête INSERT
						else{
							print("Frais inexistant : insertion du frais");
							$req2 = $cnx->prepare("INSERT INTO `lignefraisforfait`".
												 "(`idvisiteur`,`mois`,`idfraisforfait`,`quantite`)".
												 "VALUES('$idVisiteur','$mois','$libelle','$montant');");
							$req2->execute();
							print("Frais ajouté%");
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