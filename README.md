DiffTool
DiffTool is a Java-based program designed to find and highlight differences between two objects of the same type.

Features:
Comparison: Can compare nested properties of an object using dot notation.
List Handling: Can process differences within lists, including additions, removals, and updates. For lists containing objects, it can associate changes to specific object instances using identifiers.
Custom Annotations: Uses @AuditKey to determine the unique identifier of objects within a list.
Generics: Allows the user to compare objects of any custom type.

Usage:
Create an instance of DiffTool and call the diff method, passing in the previous and current states of an object:

Example:
DiffTool diffTool = new DiffTool();
List<ChangeType> changes = diffTool.diff(prevObject, currObject);

Output:
The tool outputs a list of changes. A change can be of two main types:

PropertyUpdate: Represents an update to a simple property or a nested property of an object.
-property: Name of the property using dot notation.
-previous: Previous value of the property.
-current: Current value of the property.

ListUpdate: Represents additions or removals in a list property.
-property: Name of the list property.
-added: List of added items.
-removed: List of removed items.
