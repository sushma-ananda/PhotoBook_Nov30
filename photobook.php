<?php
require_once __DIR__ . '/checkDBConn.php';

$media_url_prefix = "http://cis-linux2.temple.edu/download_file.php?filename=";

if (isset($_POST["data"])) {
	$data = json_decode($_POST["data"]);
	$photoName = $data->{'photoName'};
	$photoPath = $data->{'photoPath'};
	$photoCaption = $data->{'photoCaption'};
	$timeStamp = $data->{'timeStamp'};
	$userID = $data->{'userID'};
	//$userName = $data->{'userName'};
	$gpsLocation = $data->{'gpsLocation'};
	$locAltitude = $data->{'locAltitude'};
	$locTemp = $data->{'locTemp'};

	$response = "";
	$tmp_image_file = $_FILES["image"]["tmp_name"];
	$new_image_file =  basename($_FILES["image"]["name"]);
	copy ($tmp_image_file, "photobook_files/" . $new_image_file);
	//$check = move_uploaded_file($tmp_image_file, "../photobook_files/" . $new_image_file);
 
 	$query = "insert into PhotoExtra(gpsLocation,locAltitude,locTemp) values ('$gpsLocation', '$locAltitude', '$locTemp')";
	mysqli_query( $mysqlconn, $query);
	$detailID = mysqli_insert_id($mysqlconn);
	
	$query1 = "insert into Photo(photoPath, photoName, timeStamp, detailID, photoCaption) values ('$photoPath','$photoName', '$timeStamp', '$detailID', '$photoCaption')";
	mysqli_query($mysqlconn, $query1);
	$photoID = mysqli_insert_id($mysqlconn);
	
	$query3 = "insert into PhotoBook(photo_ID, user_ID) VALUES ('$photoID', '$userID')";
	mysqli_query($mysqlconn, $query3);
	$baseID = mysqli_insert_id($mysqlconn);
	
	$response = "{\"status\":\"ok\"}";
	
} else {
	$query = "SELECT a.photo_id, b.photoID, b.photoPath, b.photoName, b.timeStamp, b.detailID, b.photoCaption, c.gpsLocation, c.locAltitude, c.locTemp FROM PhotoBook a INNER JOIN  Photo b ON a.photo_ID = b.photoID INNER JOIN PhotoExtra c ON c.detailID = b.detailID WHERE a.user_ID = $userID";
	
	$result = mysqli_query($query);
	$response = "{\"status\":\"ok\",\"photobook\":[";
	while ($rows = mysqli_fetch_array($result)) {
			$photoURL = $media_url_prefix . $rows[2];
			$response .= "{\"photoID\":\"" . $rows[0] . "\",\"photoName\":\"" . $rows[3] . "\",\"photoPath\":\"" . $photoURL ."\",\"photoCaption\":\"" . $rows[4] . "\",\"timeStamp\":\"" . $rows[5] ."\",\"gpsLocation\":\"" . $rows[6] ."\",\"locAltitude\":\"" . $rows[7] ."\",\"locTemp\":\"" . $rows[8] . "\"},";
	}
	$response = substr($response, 0, -1) . "]}";
}

echo $response;
?>
 
