docker run --rm -v "${PWD}:/local" openapitools/openapi-generator-cli:v7.10.0 generate `
  -i /local/src/main/resources/openapi.yaml `
  -g spring `
  -o /local/generated-server `
  --additional-properties=interfaceOnly=true,skipDefaultInterface=true,useJakartaEe=true,reactive=false,packageName=ru.urasha.studygroup.generated
