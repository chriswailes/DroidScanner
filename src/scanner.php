<?php
$auth_passphrase="password";
$target_path = "root_dir/";

if(!strcmp($_POST['request'], "test")){
	if(!strcmp($_POST['pass'], $auth_passphrase)) {
		echo "TEST_SUCCESS";
	}else{
		echo "BAD AUTHENTICATION";
	}
}else if(!strcmp($_POST['request'], "upload")){
	if(!strcmp($_POST['pass'], $auth_passphrase)) {
		$target_path = $target_path . basename( $_FILES['uploadedfile']['name']);
		if(move_uploaded_file($_FILES['uploadedfile']['tmp_name'], $target_path)) {
			echo "The file ". basename( $_FILES['uploadedfile']['name']).
			" has been uploaded";
		} else{
			echo "ERROR";
		}
	}else{
		echo "BAD AUTHENTICATION";
	}
}else if (!strcmp($_POST['request'], "settings_version")){
	if(!strcmp($_POST['pass'], $auth_passphrase)) {
		echo "1"; // Version Number
	}else{
		echo "BAD AUTHENTICATION";
	}
}else if(!strcmp($_POST['request'], "read_settings")){
	if(!strcmp($_POST['pass'], $auth_passphrase)) {	
		//Order of Values: Log_Action_Bitmask,Event_Trigger_Bitmask,Min_Polling_Interval_ms
		echo "LOG_WIFI,CALL_OUTGOING,0\n"; // Settings
		echo "LOG_WIFI,CALL_INCOMING,0\n";
	}else{
		echo "BAD AUTHENTICATION";
	}
}
?>