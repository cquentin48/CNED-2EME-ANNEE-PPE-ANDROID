<?php
	if(isset($_POST)){
		switch($_POST['operationType']){
			case "connexion":
				//Cas de connexion
				$log = connexion($_POST['username'], $_POST['password']);
				break;
			case "isComptable":
				//vérifier si le compte est comptable
				$log = isComptable($_POST['user_id']);
				break;
			case "loadFrais":
				//Chargement des outils de frais
				$log = chargementFrais($_POST['user_id'], $_POST['month']);
				break;
			case "writeFrais":
				//Ecriture des outils de frais
				$log = writingFrais($_POST['listefrais'], $_POST['user_id'], $_POST['month']);
				break;
		}
		echo $log;
	}

?>