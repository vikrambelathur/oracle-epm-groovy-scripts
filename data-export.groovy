import oracle.epm.api.model.Cube
import oracle.epm.api.model.Member
import oracle.epm.api.model.RowData

Cube cube = operation.application.getCube("Plan1")

List<String> rowDimensions = ["Account","HSP_View","Scenario","Year","Version","Entity","Product"]

List<String> periods = ["Jan","Feb","Mar"]

cube.createDataExporter()
    .setColumnDimensionName("Period")
    .setColumnMemberNames(periods)
    .setRowDimensions(rowDimensions)
    .setRowFilterCriteria("BaseData","Plan","FY26","Working","000","P_000")
    .setIgnoreUnknownMembers(false)
    .exportData()
    .withCloseable { exportIterator ->

        createFilePrintWriter("Plan_Data_Slice_NoQuotes.txt")
            .withCloseable { out ->
                // Remove this line if you do not want headers
                out.println((rowDimensions + periods).join("|"))
                exportIterator.each { RowData row ->
                    List<String> outputFields =
                        new ArrayList<String>()
                    // Write row-dimension members
                    for (String dimensionName : rowDimensions) {
                        Member rowMember = (Member) row
                            .getRowTuple()
                            .getAt(dimensionName)
                        outputFields.add(
                            rowMember == null
                                ? ""
                                : rowMember.getName()
                        )
                    }

                    // Write data values
                    for (String periodName : periods) {
                        String value = row.getValue(periodName)
                        outputFields.add(
                            value == null ? "" : value
                        )
                    }

                    // Pipe-delimited output with no double quotes
                    out.println(outputFields.join("|"))
                }
            }
    }