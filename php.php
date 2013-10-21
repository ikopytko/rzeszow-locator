<?php

if ($_SERVER['REQUEST_METHOD'] == 'POST' &&
	isset($_POST['name'], $_POST['descr'], $_POST['lat'], $_POST['lon'])) {

	$uploaddir = '/home/vol14_2/byethost7.com/b7_13892182/htdocs/';
	$uploadfile = $uploaddir . basename($_FILES['userfile']['name']);
	
	$title = htmlspecialchars($_POST['name']);
	$descr = htmlspecialchars($_POST['descr']);
	$lat = htmlspecialchars($_POST['lat']);
	$lon = htmlspecialchars($_POST['lon']);
	
	if (move_uploaded_file($_FILES['userfile']['tmp_name'], $uploadfile)) {
	  echo "0<pre><span style=\"background-color:green\">Файл корректен и был успешно загружен.\n</span>";
	} else {
	  echo "<pre><span style=\"background-color:yellow\">Возможная атака с помощью файловой загрузки!\n</span>";
	}
	
	$entery = 
	      ",\n{\n    \"name\": \"".$title.
		"\",\n    \"description\": \"".$descr.
		"\",\n    \"simg\": \"".$_FILES['userfile']['name'].
		"\",\n    \"bimg\": \"".$_FILES['userfile']['name'].
		"\",\n    \"lat\": \"".$lat.
		"\",\n    \"lon\": \"".$lon."\"\n}\n]";
	echo $entery."\n";
	
	$filename = "/home/vol14_2/byethost7.com/b7_13892182/htdocs/a.json";
	$handle = fopen($filename, "c+b");
	
	if ($handle) {
		echo "<span style=\"background-color:green\">File a.json was opened.\n</span>";

	fseek($handle, (filesize($filename)-3));
	
	fwrite($handle, $entery);
	
	while (($buffer = fgets($handle)) !== false) {
		echo $buffer;
	}

	fclose($handle);
	} else {
	  echo "<span style=\"background-color:yellow\">File a.json was not opened!\n</span>";
	}
	
	print "</pre>";
  
} else {
?>


<form enctype="multipart/form-data" action="<?=$_SERVER['PHP_SELF']?>" method="POST">
<table>
  <tr>
	<td>Title</td>
	<td><input type="text" name="name"></td>
  </tr>
  <tr>
	<td>Description</td>
	<td><input type="text" name="descr"></td>
  </tr>
  <tr>
	<td>Latitude</td>
	<td><input type="text" name="lat"></td>
  </tr>
  <tr>
	<td>Longitude</td>
	<td><input type="text" name="lon"></td>
  </tr>
  <tr>
	<td>Picture</td>
	<td><input name="userfile" type="file" /></td>
  </tr>
  <tr> 
	<td colspan="2"><input type="submit" value="Add place" /></td>
  </tr>
</table>
</form>

<?php
}
?>