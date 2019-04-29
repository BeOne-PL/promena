var node = search.findNode("workspace://SpacesStore/c0b95525-26a6-4067-9756-6bec11c93c70");
var node2 = search.findNode("workspace://SpacesStore/6967ef69-2768-411e-8ab4-4dc66001911b");

barcodeTransformationServiceJscript.splitPdf(node, node2, null, "^LD[0-9]{5}$", null, ["CODABAR", "CODE_93"]);

// ***

var node = search.findNode("workspace://SpacesStore/f0ee3818-9cc3-4e4d-b20b-1b5d8820e133");
var node2 = search.findNode("workspace://SpacesStore/98c8a344-7724-473d-9dd2-c7c29b77a0ff");

converterTransformationServiceJscript.convert(node, node2, "application/pdf");

// ***

var node = search.findNode("workspace://SpacesStore/68462d80-70d4-4b02-bda2-be5660b2413e");
var node2 = search.findNode("workspace://SpacesStore/a36d5c1a-e32c-478b-ad8b-14b2882115d1");

//ocrTransformationServiceJscript.addTextLayerToPdf(node, node2, 3);
//ocrTransformationServiceJscript.addTextLayerToPdf(node, node2, null);

//ocrTransformationServiceJscript.getPlainTextFromPdf(node, node2, null, null);
//ocrTransformationServiceJscript.getPlainTextFromPdf(node, node2, 3, true);
ocrTransformationServiceJscript.getPlainTextFromPdf(node, node2, 3, false);